var Vaska = (function (global) {
  'use strict';

  var LinkPrefix = '@link:',
    ArrayPrefix = '@arr:',
    ObjPrefix = '@obj:',
    UnitResult = "@unit",
    HookSuccess = "@hook_success",
    HookFailure = "@hook_failure",
    LinkNotFound = "Link no found",
    JSMimeType = {
      type: 'application/javascript'
    };

  function loadScript(url, progressCb) {
    return new Promise(function (resolve, reject) {
      var http = new XMLHttpRequest();
      http.open('GET', url, true);
      http.addEventListener('load', resolve);
      http.addEventListener('error', reject);
      http.addEventListener('progress', function (oEvent) {
        if (progressCb) {
          progressCb(oEvent.loaded / oEvent.total);
        }
      });
      http.send();
    });
  }

  function Vaska(postMessage, plugins) {
    var self = this,
      initialize = null,
      lastLinkId = 0,
      weakLinks = null,
      links = new Map();
    if (global.WeakMap !== undefined) {
      weakLinks = new WeakMap();
    } else {
      // TODO print warning when scala.js will be updated to 0.6.3
      weakLinks = new Map();
    }
    links.set('plugins', plugins);

    function createLink(obj) {
      var id = lastLinkId;
      lastLinkId += 1;
      weakLinks.set(obj, id.toString());
      return id;
    }

    function findWeakLink(id) {
      var keysIter = weakLinks.keys(),
        valuesIter = weakLinks.values();
      while (!keysIter.done) {
        if (valuesIter.value === id) {
          return keysIter.value;
        }
        keysIter = keysIter.next();
        valuesIter = valuesIter.next();
      }
      return;
    }

    function unpackArgs(args) {
      var l = args.length,
        i = 0,
        arg = null,
        id = null,
        weakLink = null;
      for (i = 0; i < l; i += 1) {
        arg = args[i];
        if (typeof arg === "string" && arg.indexOf(LinkPrefix) === 0) {
          id = arg.substring(LinkPrefix.length);
          weakLink = findWeakLink(id);
          if (weakLink !== undefined) {
            args[i] = weakLink;
          } else {
            args[i] = links.get(id);
          }
        }
      }
      return args;
    }

    function packResult(arg) {
      if (typeof arg === 'object') {
        var id = weakLinks.get(arg);
        if (id === undefined) {
          id = createLink(arg);
        }
        if (arg instanceof Array) {
          return ArrayPrefix + id;
        }
        return ObjPrefix + id;
      }
      return arg;
    }

    function createHook(reqId, success) {
      return function VaskaHook(res) {
        postMessage([reqId, success, packResult(res)]);
      };
    }

    this.checkLinkExists = function (id) {
      return unpackArgs([LinkPrefix + id])[0] !== undefined;
    };

    this.checkLinkSaved = function (id) {
      return links.has(id);
    };

    this.initialized = new Promise(function (resolve) {
      initialize = resolve;
    });

    this.receive = function (data) {
      var reqId = data[0],
        method = data[1],
        args = unpackArgs(data.slice(2));
      switch (method) {
        // Misc
      case 'init':
        initialize(self);
        postMessage([reqId, true, UnitResult]);
        break;
      // Link methods
      case 'save':
        (function VaskaReceiveSave() {
          var obj = args[0],
            newId = args[1];
          if (obj) {
            links.set(newId, obj);
            postMessage([reqId, true, packResult(obj)]);
          } else {
            postMessage([reqId, false, LinkNotFound]);
          }
        })();
        break;
      case 'free':
        (function VaskaReceiveFree() {
          var obj = args[0],
            id = null;
          if (obj) {
            id = weakLinks.get(obj);
            links.delete(id);
            weakLinks.delete(obj);
            postMessage([reqId, true, UnitResult]);
          } else {
            postMessage([reqId, false, LinkNotFound]);
          }
        })();
        break;
      // Object methods
      case 'get':
        (function VaskaReceiveGet() {
          var obj = args[0],
            res = null,
            err = null;
          if (obj) {
            res = obj[args[1]];
            if (res === undefined) {
              err = obj + "." + args[1] + " is undefined";
              postMessage([reqId, false, err]);
            } else {
              postMessage([reqId, true, packResult(res)]);
            }
          } else {
            postMessage([reqId, false, LinkNotFound]);
          }
        })();
        break;
      case 'set':
        (function VaskaReceiveSet() {
          var obj = args[0],
            name = null,
            value = null;
          if (obj) {
            name = args[1];
            value = args[2];
            obj[name] = value;
            postMessage([reqId, true, UnitResult]);
          } else {
            postMessage([reqId, false, LinkNotFound]);
          }
        })();
        break;
      case 'call':
        (function VaskaReceiveCall() {
          var obj = args[0],
            res = null,
            err = null,
            name = null,
            callArgs = null,
            hasHooks = false,
            arg = null,
            i = 0;
          if (!obj) {
            postMessage([reqId, false, LinkNotFound]);
            return;
          }
          name = args[1];
          callArgs = args.slice(2);
          for (i = 0; i < callArgs.length; i += 1) {
            arg = callArgs[i];
            if (arg === HookSuccess) {
              callArgs[i] = createHook(reqId, true);
              hasHooks = true;
            } else if (arg === HookFailure) {
              callArgs[i] = createHook(reqId, false);
              hasHooks = true;
            }
          }
          try {
            if (hasHooks) {
              obj[name].apply(obj, callArgs);
            } else {
              res = packResult(obj[name].apply(obj, callArgs));
              if (res === undefined) {
                res = UnitResult;
              }
              postMessage([reqId, true, res]);
            }
          } catch (exception) {
            err = obj + '.' + name + '(' + callArgs + ') call failure';
            postMessage([reqId, false, err]);
            throw exception;
          }
        })();
        break;
      }
    };
  }

  return {

    /**
     * Run Scala.js compiled application in the 
     * same thread as DOM runs
     */
    basic: function (mainClass, scriptUrl) {
      return new Promise(function (resolve, reject) {
        loadScript(scriptUrl).then(function (applicationCode) {
          var applicationBlob = new Blob([applicationCode], JSMimeType),
            tag = document.createElement("script");
          tag.setAttribute('src', URL.createObjectURL(applicationBlob));
          tag.addEventListener('load', function () {
            var scope = {},
              jsAccess = new vaska.NativeJSAccess(scope),
              application = new global[mainClass](),
              vaskaObj = new Vaska(scope.onmessage);
            scope.postMessage = function (data) {
              vaskaObj.receive(data);
            };
            application.start(jsAccess);
            resolve(vaskaObj);
          });
          document.body.appendChild(tag);
        }).catch(reject);
      });
    },

    /**
     * Run Scala.js compiled application in the
     * same thread as DOM runs
     */
    worker: function (mainClass, scriptUrl) {
      return new Promise(function (resolve, reject) {
        loadScript(scriptUrl).then(function (workerCode) {
          var workerBlob = null,
            launcherBlob = null,
            worker = null,
            vaska = null;
          workerBlob = new Blob([workerCode], JSMimeType);
          launcherBlob = new Blob([
            'importScripts("' + URL.createObjectURL(workerBlob) + '");',
            'var jsAccess = new vaska.NativeJSAccess();',
            'var application = new ' + mainClass + '();',
            'application.start(jsAccess);'
          ], JSMimeType);
          // Run launcher in WebWorker
          worker = new Worker(URL.createObjectURL(launcherBlob));
          vaska = new Vaska(worker.postMessage);
          worker.addEventListener('message', vaska.receive);
          vaska.initialized.then(function () {
            resolve(vaska);
          });
        }).catch(reject);
      });
    },

    /**
     * Connect to remote server via WebSocket
     */
    webSocket: function (url) {
      return new Promise(function (resolve, reject) {
        var ws = new WebSocket(url),
          vaska = new Vaska(ws.send);
        ws.addEventListener('message', vaska.receive);
        ws.addEventListener('error', reject);
        vaska.initialized.then(function () {
          resolve(vaska);
        });
      });
    },

    create: function (postMessage, plugins) {
      return new Vaska(postMessage, plugins);
    }
  };
}(this));
