var Vaska = (function (global) {
  'use strict';

  var workerProtocolDebugEnabled = (localStorage.getItem("$vaska.workerProtocolDebugEnabled") === 'true'),
    tmpLinkLifetime = parseInt(localStorage.getItem("$vaska.tmpLinkLifetime")) || 5000,
    LinkPrefix = '@link:',
    SourceMappingPattern = '//# sourceMappingURL=',
    ArrayPrefix = '@arr:',
    ObjPrefix = '@obj:',
    UnitResult = "@unit",
    NullResult = "@null",

    HookSuccess = "@hook_success",
    HookFailure = "@hook_failure",

    LinkNotFound = "Link no found",
    JSMimeType = {
      type: 'application/javascript'
    };

  function Transferable(value) {
    this.value = value
  }

  function Vaska(postMessageFunction, testEnv) {
    function postMessage(data) {
      var res = data[2], value;
      if (res instanceof Transferable) {
        value = res.value
        data[2] = value;
        postMessageFunction(data, [value]);
      }
      else postMessageFunction(data);
    }
    var self = this,
      initialize = null,
      lastLinkId = 0,
      tmpLinks = new Map(),
      tmpLinksIndex = new Map(),
      tmpLinksTime = new Map(),
      links = new Map(),
      linksIndex = new Map();

    links.set('global', global);
    links.set('testEnv', testEnv);
    linksIndex.set(global, 'global')
    linksIndex.set(testEnv, 'testEnv');

    function createTmpLink(obj) {
      var id = lastLinkId.toString();
      lastLinkId++;
      tmpLinks.set(id, obj);
      tmpLinksIndex.set(obj, id);
      tmpLinksTime.set(id, Date.now());
      return id;
    }

    function getLink(id) {
      var tmpLink = tmpLinks.get(id);
      if (tmpLink !== undefined) {
        return tmpLink;
      }
      return links.get(id);
    }

    function unpackArgs(args) {
      var l = args.length,
        i = 0,
        arg = null,
        id = null,
        tpe = null;
      for (i = 0; i < l; i += 1) {
        arg = args[i];
        tpe = typeof arg;
        if (tpe === 'string' && arg.indexOf(LinkPrefix) === 0) {
          id = arg.substring(LinkPrefix.length);
          args[i] = getLink(id);
        }
        else if (tpe === 'object' && arg instanceof Array) {
          unpackArgs(arg)
        }
      }
      return args;
    }

    function packResult(arg) {
      if (arg === undefined) {
        return UnitResult;
      }
      if (arg === null) {
        return NullResult;
      }
      if (arg instanceof Transferable) {
        return arg
      }
      if (typeof arg === 'object') {
        var id = tmpLinksIndex.get(arg) || linksIndex.get(arg);
        if (id === undefined) {
          id = createTmpLink(arg);
        }
        if (arg instanceof Array) {
          return ArrayPrefix + id;
        }
        return ObjPrefix + id;
      }
      return arg;
    }

    function createHook(reqId, success, cb) {
      return function hook(res) {
        cb([reqId, success, packResult(res)]);
      };
    }

    function receiveCall(reqId, args, cb) {
      var obj = args[0],
        res = null,
        err = null,
        name = null,
        callArgs = null,
        hasHooks = false,
        arg = null,
        i = 0;
      if (!obj) {
        cb([reqId, false, LinkNotFound]);
        return;
      }
      name = args[1];
      callArgs = args.slice(2);
      for (i = 0; i < callArgs.length; i++) {
        arg = callArgs[i];
        if (arg === HookSuccess) {
          callArgs[i] = createHook(reqId, true, cb);
          hasHooks = true;
        } else if (arg === HookFailure) {
          callArgs[i] = createHook(reqId, false, cb);
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
          cb([reqId, true, res]);
        }
      } catch (exception) {
        err = obj + '.' + name + '(' + callArgs + ') call failure';
        cb([reqId, false, err]);
      }
    }

    function receiveSave(reqId, args, cb) {
      var obj = args[0],
        newId = args[1];
      if (obj) {
        links.set(newId, obj);
        linksIndex.set(obj, newId);
        cb([reqId, true, packResult(obj)]);
      } else {
        cb([reqId, false, LinkNotFound]);
      }
    }

    function receiveCallAndSaveAs(reqId, args, cb) {
      var newId = args[2],
        id = null,
        err = null;
      args = args.slice(0,2).concat(args.slice(3))
      receiveCall(reqId, args, function (callRes) {
        callRes = callRes[2]
        if (callRes.indexOf(ObjPrefix) !== -1) {
          id = callRes.substring(ObjPrefix.length);
          receiveSave(reqId, [getLink(id), newId], cb);
        } else {
          err = args[1] + ' returns ' + (typeof callRes);
          cb([reqId, false, err]);
        }
      });
    }

    function receiveGet(reqId, args, cb) {
      var obj = args[0],
        res = null,
        err = null;
      if (obj) {
        res = obj[args[1]];
        if (res === undefined) {
          err = obj + "." + args[1] + " is undefined";
          cb([reqId, false, err]);
        } else {
          cb([reqId, true, packResult(res)]);
        }
      } else {
        cb([reqId, false, LinkNotFound]);
      }
    }

    function receiveGetAndSaveAs(reqId, args, cb) {
      var newId = args[2];
      receiveGet(reqId, args, function (callRes) {
        var id = null, err;
        callRes = callRes[2]
        if (callRes.indexOf(ObjPrefix) !== -1) {
          id = callRes.substring(ObjPrefix.length);
          receiveSave(reqId, [getLink(id), newId], cb);
        } else {
          err = args[1] + ' returns ' + (typeof callRes);
          cb([reqId, false, err]);
        }
      });
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
      if (workerProtocolDebugEnabled) console.log('->', data);
      var reqId = data[0],
        method = data[1],
        rawArgs = data.slice(2),
        args = unpackArgs(rawArgs.concat()),
        tmp = null;
      switch (method) {
        // Misc
      case 'init':
        initialize(self);
        postMessage([reqId, true, UnitResult]);
        setInterval(function () {
          var result = 0;
          tmpLinksIndex.forEach(function (id) {
            var dt = Date.now() - tmpLinksTime.get(id), obj;
            if (dt > tmpLinkLifetime) {
              obj = tmpLinks.get(id);
              tmpLinks.delete(id);
              tmpLinksTime.delete(id);
              tmpLinksIndex.delete(obj);
              result++;
            }
          });
        }, tmpLinkLifetime);
        break;
      case 'registerCallback':
        (function VaskaRegisterCallback() {
          var callbackId = args[0];
          function callback(arg) {
            postMessage([-1, callbackId, packResult(arg)]);
          }
          links.set(callbackId, callback);
          linksIndex.set(callback, callbackId);
          postMessage([reqId, true, ObjPrefix + callbackId]);
        })();
        break;
      // Link methods
      case 'save':
        receiveSave(reqId, args, postMessage);
        break;
      case 'free':
        (function VaskaReceiveFree() {
          var obj = args[0],
            id = rawArgs[0].replace(LinkPrefix, '');
          if (obj) {
            links.delete(id);
            tmpLinks.delete(id);
            postMessage([reqId, true, UnitResult]);
          } else {
            postMessage([reqId, false, LinkNotFound]);
          }
        })();
        break;
      // Object methods
      case 'getAndSaveAs':
        receiveGetAndSaveAs(reqId, args, postMessage);
        break;
      case 'get':
        receiveGet(reqId, args, postMessage);
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
        receiveCall(reqId, args, postMessage);
        break;
      case 'callAndSaveAs':
        receiveCallAndSaveAs(reqId, args, postMessage);
        break;
      }
    };
  };

  return {

    /**
     * Run Scala.js compiled application in the
     * same thread as DOM runs
     */
    basic: function (mainClass, scriptUrl) {
      return new Promise(function (resolve, reject) {
        var tag = document.createElement('script');
          tag.setAttribute('src', scriptUrl);

          tag.addEventListener('load', function () {
            var scope = {},
              jsAccess = new vaska.NativeJSAccess(scope),
              vaskaObj = new Vaska(function (data) {
                scope.onmessage({data : data});
              });

            scope.postMessage = function (data) {
              vaskaObj.receive(data);
            };

            eval(mainClass)().main(jsAccess);
            resolve(vaskaObj);
          });

          document.addEventListener('DOMContentLoaded', function() {
            document.head.appendChild(tag);
          });
      });
    },

    /**
     * Run Scala.js compiled application in the
     * same thread as DOM runs
     */
    worker: function (mainClass, scriptUrl, dependencies) {
      var toAbsoluteUrl = function (url) {
        var parser = document.createElement('a');
        parser.href = url;
        return parser.href;
      };

      if (typeof dependencies === "string") dependencies = [dependencies];
      if (!dependencies || dependencies instanceof Array === false) dependencies = [];

      var scripts = dependencies.map(toAbsoluteUrl);
      scripts.push(toAbsoluteUrl(scriptUrl));

      return new Promise(function (resolve, reject) {
        var injectedJS = ('importScripts(\'{0}\');\n' +
                          'console.log("Scripts imported to worker");\n' +
                          'var jsAccess = new vaska.NativeJSAccess(this);' +
                          '{1}().main(jsAccess);' +
                          'console.log("Application started inside worker");')
                            .replace('{0}', scripts.join('\', \''))
                            .replace('{1}', mainClass);

        var launcherBlob = new Blob([injectedJS], JSMimeType);

        // Run launcher in WebWorker
        var worker = new Worker(URL.createObjectURL(launcherBlob));

        var vaska = new Vaska(function(data, transferable) {
          if (workerProtocolDebugEnabled) {
            console.log('<-', data, transferable);
          }
          worker.postMessage(data, transferable);
        });

        worker.addEventListener('message', function(event) {
          vaska.receive(event.data);
        });

        vaska.initialized.then(function () {
          resolve(vaska);
        });
      });
    },

    /**
     * Connect to remote server via WebSocket
     */
    webSocket: function (url) {
      return new Promise(function (resolve, reject) {
        var ws = new WebSocket(url),
          vaska = new Vaska(function (data) {
            ws.send(JSON.stringify(data));
          });
        ws.addEventListener('message', function (event) {
          vaska.receive(JSON.parse(event.data));
        });
        ws.addEventListener('error', reject);
        vaska.initialized.then(function () {
          resolve(vaska);
        });
      });
    },

    create: function (postMessage, testEnv) {
      return new Vaska(postMessage, testEnv);
    },

    Transferable: Transferable,

    setWorkerProtocolDebugEnabled: function(value) {
      localStorage.setItem("$vaska.workerProtocolDebugEnabled", value);
      workerProtocolDebugEnabled = value;
    },
    setTmpLinkLifetime: function(value) {
      localStorage.setItem("$vaska.tmpLinkLifetime", value);
      console.log('Restart application to apply changes');
    }
  };
}(this));
