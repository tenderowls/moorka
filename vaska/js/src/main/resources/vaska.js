var Vaska = (function() {

  var BreakException = {};
  var LinkPrefix = '@link:';
  var ArrayPrefix = '@arr:';
  var ObjPrefix = '@obj:';
  
  var UnitResult = "@unit";
  var HookSuccess = "@hook_success";
  var HookFailure = "@hook_failure";
  
  var LinkNotFound = "Link no found";
  
  var JSMimeType = {
    type: 'application/javascript'
  };

  function loadScript(url, progressCb) {
    return new Promise(function(resolve, reject) {
      var http = new XMLHttpRequest();
      http.open('GET', url, true);
      http.addEventListener('load', resolve);
      http.addEventListener('error', reject);
      http.addEventListener('progress', function() {
        if (progressCb) {
          progressCb(oEvent.loaded / oEvent.total);
        }
      });
      http.send();
    });
  }

  function Vaska(postMessage, plugins) {
  
    var self = this;
    var initialize = null;
    
    var lastLinkId = 0;
    var weakLinks = null;
    var links = new Map();  
    
    if (typeof WeakMap !== "undefined") {
      weakLinks = new WeakMap();
    }
    else {
      // TODO print warning when scala.js will be updated to 0.6.3
      weakLinks = new Map();
    }
           
    links.set('plugins', plugins);

    function createLink(obj) {
      var id = lastLinkId++;
      weakLinks.set(obj, id.toString());
      return id; 
    }
    
    function unpackArgs(args) {
      var l = args.length;
      for (var i = 0; i < l; i++) {
        var arg = args[i];
        if (typeof arg === "string" && arg.indexOf(LinkPrefix) === 0) {
          var id = arg.substring(LinkPrefix.length);
          var weakLinkFound = false;
          try {
            weakLinks.forEach(function(objId, obj) {
              if (objId === id) {
                args[i] = obj;
                weakLinkFound = true
                throw BreakException;
              }
            });
            if (!weakLinkFound)
              args[i] = links.get(id);
          }
          catch (exception) {
            if (exception !== BreakException) 
              throw exception;
          }
        }
      }
      return args;
    }
    
    function packResult(arg) {
      if (typeof arg === 'object') {
        var id = weakLinks.get(arg);
        if (typeof id === 'undefined') id = createLink(arg);
        if (arg instanceof Array) return ArrayPrefix + id;
        else return ObjPrefix + id;
      }
      else {
        return arg;
      }
    }
    
    this.checkLinkExists = function(id) {
      return typeof unpackArgs([LinkPrefix + id])[0] !== 'undefined'
    }

    this.checkLinkSaved = function(id) {
      return typeof links.get(id) !== 'undefined'
    }

    this.initialized = new Promise(function(resolve, _) {
      initialize = resolve
    });

    this.receive = function(data) {
      var reqId = data[0];
      var method = data[1];
      var args = unpackArgs(data.slice(2));
      switch (method) {
        // Misc
        case 'init':
          initialized(self);
          postMessage([reqId, true, UnitResult])
          break;
        // Link methods
        case 'save':
          (function VaskaReceiveSave() {
            var obj = args[0];
            var newId = args[1];
            if (obj) {
              links.set(newId, obj);
              postMessage([reqId, true, packResult(obj)]);
            }
            else postMessage([reqId, false, LinkNotFound]);
          })();
          break;
        case 'free':
          (function VaskaReceiveFree() {
            var obj = args[0];
            if (obj) {
              var id = weakLinks.get(obj);
              links.delete(id);
              weakLinks.delete(obj);
              postMessage([reqId, true, UnitResult]);
            }
            else postMessage([reqId, false, LinkNotFound]);
          })();
          break;
        // Object methods
        case 'get':
          (function VaskaReceiveGet() {
            var obj = args[0];
            if (obj) {
              var res = obj[args[1]];
              if (typeof res === "undefined") {
                var err = obj + "." + args[1] + " is undefined";
                postMessage([reqId, false, err]);
              }
              else postMessage([reqId, true, packResult(res)]);
            }
            else postMessage([reqId, false, LinkNotFound]);
          })();
          break;
        case 'set':
          (function VaskaReceiveSet() {
            var obj = args[0];
            if (obj) {
              var name = args[1];
              var value = args[2];
              obj[name] = value;
              postMessage([reqId, true, UnitResult]);
            }
            else postMessage([reqId, false, LinkNotFound]);
          })();
          break;
        case 'call': 
          (function VaskaReceiveCall() {
            var obj = args[0];
            if (!obj) {
              postMessage([reqId, false, LinkNotFound]);
              return;
            }
            var name = args[1];
            var callArgs = args.slice(2);
            var hasHooks = false;
            for (var i = 0; i < callArgs.length; i++) {
              var arg = callArgs[i];
              if (arg == HookSuccess) {
                callArgs[i] = function VaskaHookSuccess(res) {
                  postMessage([reqId, true, packResult(res)]);
                }
              }
              else if (arg == HookFailure) {
                callArgs[i] = function VaskaHookFailure(err) {
                  postMessage([reqId, false, packResult(err)]);
                }
              }
            }
            try {
              if (hasHooks) {
                obj[name].apply(obj, callArgs);
              }
              else {
                var res = packResult(obj[name].apply(obj, callArgs));
                if (typeof res === 'undefined') res = UnitResult;
                postMessage([reqId, true, res]);
              }
            }
            catch(exception) {
              var err = obj + '.' + name + '('+callArgs+') call failure';
              postMessage([reqId, false, '']);
              throw exception;
            }
          })();
          break;
      }
    }
  }
  
  return $o = {
  
    /**
     * Run Scala.js compiled application in the 
     * same thread as DOM runs
     */
    basic: function(mainClass, scriptUrl) {
      return new Promise(function(resolve, reject) {
        loadScript(scriptUrl).then(function(applicationCode) {
          var applicationBlob = new Blob([applicationCode], JSMimeType);
          var tag = document.createElement("script")
          tag.setAttribute('src', URL.createObjectURL(applicationBlob))
          tag.addEventListener('load', function() {
            var scope = {};
            var jsAccess = new vaska.NativeJSAccess(scope);
            var application = eval('new ' + mainClass + '()');
            var vaska = new Vaska(scope.onmessage);
            scope.postMessage = function(data) {
              vaska.receive(data)
            };
            application.start(jsAccess);
            resolve(vaska);
          });
          document.body.appendChild(tag);
        }).catch(reject);
      });
    },
    
    /**
     * Run Scala.js compiled application in the
     * same thread as DOM runs
     */
    worker: function(mainClass, scriptUrl) {
      return new Promise(function(resolve, reject) {
        loadScript(scriptUrl).then(function(workerCode) {
          var workerBlob = new Blob([workerCode], JSMimeType);
          var launcherBlob = new Blob([
            'importScripts("' + URL.createObjectURL(workerBlob) + '");',
            'var jsAccess = new vaska.NativeJSAccess();',
            'var application = new ' + mainClass + '();',
            'application.start(jsAccess);'
          ], JSMimeType);
          // Run launcher in WebWorker
          var worker = new Worker(URL.createObjectURL(launcherBlob));
          var vaska = new Vaska(worker.postMessage);
          worker.addEventListener('message', vaska.receive);
          vaska.initialized.then(function() {
            resolve(vaska);
          });
        }).catch(reject);
      });
    },
    
    /**
     * Connect to remote server via WebSocket
     */
    webSocket: function(url) {
      return new Promise(function(resolve, reject) {
        var ws = new WebSocket(url);
        var vaska = new Vaska(ws.send);
        ws.addEventListener('message', vaska.receive);
        ws.addEventListener('error', reject);
        vaska.initialized.then(function() {
          resolve(vaska);
        });
      });
    },
    
    create: function(postMessage, plugins) {
      return new Vaska(postMessage, plugins)
    }
  }
  
})();

