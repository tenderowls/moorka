function Moorka() {

  function RenderBackendImpl() {

    var self = this;

    var entities = { "root": document.body };

    function element(refId) {
      return entities[refId];
    }

    function copyEvent(e) {
      var copy = {};
      copy.type = e.type
      copy.target = e.target.id
      switch (e.constructor) {
        case MouseEvent: {
          copy.altKey = e.altKey;
          copy.ctrlKey = e.ctrlKey;
          copy.metaKey = e.metaKey;
          copy.button = e.button;
          copy.clientX = e.clientX;
          copy.clientY = e.clientY;
          copy.screenX = e.screenX;
          copy.screenY = e.screenY;
          break;
        }
      }
      return copy;
    }

    this.send = null

    this.receive = function (ops) {
      var l = ops.length;
      for (var i = 0; i < l; i++) {
        var op = ops[i];
        switch (op[0]) {
          case "create_ref" :
            var factoryId = op[1];
            var refId = op[2];
            var el = document.createElement(factoryId);
            el.id = refId;
            entities[refId] = el;
            break;
          case "kill_ref":
            console.log(op[0], op[1]);
            delete entities[op[1]];
            break;
          case "append_child" :
            // (to, element)
            var elementId = op[1];
            var newChildId = op[2];
            element(elementId).appendChild(element(newChildId));
            break;
          case "insert_child" :
            var toId = op[1];
            var newId = op[2];
            var beforeId = op[3];
            element(toId).insertBefore(element(newId), element(beforeId));
            break;
          case "remove_child" :
            var fromId = op[1];
            var elementId = op[2];
            element(fromId).removeChild(element(elementId));
            break;
          case "replace_child":
            var whereId = op[1];
            var newChildId = op[2];
            var oldChildId = op[3];
            element(whereId).replaceChild(element(newChildId), element(oldChildId));
            break;
          case "remove_children":
            var from = element(op[1]);
            var xs = op[2];
            xs.forEach(function (x) {
              from.removeChild(element(x))
            });
            break;
          case "append_children":
            var xs = op[2];
            if (xs.length > 0) {
              var fragment = document.createDocumentFragment();
              var to = element(op[1]);
              xs.forEach(function (x) {
                fragment.appendChild(element(x))
              });
              to.appendChild(fragment)
            }
            break;
          case "update_attribute" :
            var elementId = op[1];
            element(elementId).setAttribute(op[2], op[3]);
            break;
          case "class_add" :
            var elementId = op[1];
            element(elementId).classList.add(op[2]);
            break;
          case "class_remove" :
            var elementId = op[1];
            element(elementId).classList.remove(op[2]);
            break;
          case "set" :
            var elementId = op[1];
            var propertyName = op[2];
            element(elementId)[propertyName] = op[3];
            break;
          case "get" :
            //(element, name, reqId)
            var elementId = op[1];
            var propName = op[2];
            var value = element(elementId)[propName];
            self.send(["get_response", op[3], value]);
            break;
          case "call" :
            //(element, name, reqId, args ...)
            var el = element(op[1]);
            var funcName = op[2];
            var opCount = op.length;
            var result = null;
            if (opCount > 4) {
              result = el[funcName].apply(el, op.slice(4).map(function (arg) {
                if (arg instanceof String) {
                  if (arg.substring(0, 3) == "$$:")
                    return element(arg.substring(3));
                }
                return arg
              }));
            }
            else {
              result = el[funcName]()
            }
            self.send(["get_response", op[3], result])
        }
      }
    };

    ["click", "dblclick", "change"].forEach(function (eventType) {
      document.addEventListener(eventType, function (event) {
        self.send(["event", copyEvent(event)]);
      });
    });

    // Auto-cancel submit event
    document.addEventListener("submit", function (event) {
      event.preventDefault();
      self.send(["event", copyEvent(event)])
    });
  }

  this.application = function(main, script) {
    var bootstrap = document.createElement("script");
    bootstrap.onload = function() {
      var renderBackend = new RenderBackendImpl();
      renderBackend.send = renderBackendApi().defaultMode(renderBackend.receive);
      renderBackend.send(["start"]);
      eval(main + "().main()");
    };
    bootstrap.setAttribute("src", script);
    document.body.appendChild(bootstrap);
  };

  this.workerApplication = function (main, script) {
    var workerScript = [
      "importScripts('" + script + "');\n",
      "renderBackendApi().workerMode();\n",
      main + "().main();"
    ];
    var blob = new Blob(workerScript, { "type": "application/javascript" });
    var worker = new Worker(URL.createObjectURL(blob));
    var renderBackend = new RenderBackendImpl();
    renderBackend.send = function(x) {
      worker.postMessage(x)
    };
    worker.onmessage = function(x) {
      renderBackend.receive(x.data);
    };
    worker.postMessage(["start"]);
  }
}