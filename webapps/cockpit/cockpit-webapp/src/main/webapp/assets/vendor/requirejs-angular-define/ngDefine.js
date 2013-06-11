/**
 * ngDefine() - a friendly integration of AngularJS into RequireJS powered applications
 *
 * @version 1.0.0
 * @author Nico Rehwaldt <http://github.com/Nikku>
 *
 * @license (c) 2013 Nico Rehwaldt, MIT
 */
(function(window) {

  var ngDefine;

  var MODULE_DEPENDENCY = /^module:([^:]*)(:(.*))?$/;
  var INTERNAL = /^ng/;

  var isInternal = function(module) {
    return INTERNAL.test(module);
  };

  var asFileDependency = function(module) {
    return module.replace(/\./g, "/");
  };

  var toArray = function(arrayLike) {
    return Array.prototype.slice.call(arrayLike, 0);
  };

  var internalModule = function(angular, name, dependencies, body) {

    var each = angular.forEach;

    var files = [],
        modules = [];

    if (!body) {
      body = dependencies;
      dependencies = null;
    }

    each(dependencies || [], function(d) {
      var moduleMatch = d.match(MODULE_DEPENDENCY);
      if (moduleMatch) {
        var module = moduleMatch[1],
            path = moduleMatch[3];

        if (!path && !isInternal(module)) {
          // infer path from module name
          path = asFileDependency(module);
        }

        // add module dependency
        modules.push(module);

        if (path) {
          // add path dependency if it exists
          files.push(path);
        }
      } else {
        files.push(d);
      }
    });

    var module, exists;

    try {
      angular.module(name);
      exists = true;
    } catch (e) {
      exists = false;
    }

    if (modules.length && exists) {
      throw new Error(
        "Cannot re-define angular module " + name + " with new dependencies [" + modules + "]. " +
        "Make sure the module is not defined else where or define a sub-module with additional angular module dependencies instead.");
    }

    if (modules.length || !exists) {
      module = angular.module(name, modules);
      debugLog(name, "defined with dependencies", modules);
    } else {
      module = angular.module(name);
      debugLog(name, "looked up");
    }

    define(files, function() {
      var results = toArray(arguments);
      results.unshift(module);

      body.apply(window, results);

      debugLog(name, "loaded");
      return module;
    });
  };

  /**
   * Angular module definition / lookup
   *
   * @param  {string}             name of the module
   * @param  {string}             (optional) dependencies of the module
   * @param  {Function}           body function defining the module
   */
  var angularDefine = function(angular) {
    return function(name, dependencies, body) {
      if (!dependencies) {
        throw new Error("wrong number of arguments, expected name[, dependencies], body");
      }
      internalModule(angular, name, dependencies, body);
    };
  };

  var debugLog;

  // for logging only
  (function() {
    var log;

    // IE 9 logging #!?.
    if (Function.prototype.bind && window.console && window.console.log) {
      log = Function.prototype.bind.call(window.console.log, window.console);
    }

    debugLog = function() {
      if (!ngDefine.debug || !log) {
        return;
      }

      var args = toArray(arguments);
      args.unshift("[ngDefine]");

      log.apply(log, args);
    };
  })();

  define([ 'angular' ], function(angular) {

    ngDefine = ngDefine || angularDefine(angular);

    if (!window.ngDefine) {
      window.ngDefine = ngDefine;
    }

    // publish as requireJS module
    return ngDefine;
  });

  // publish statically in case we use the module outside
  // a requirejs context (e.g. during testing)
  if (window.angular) {
    window.ngDefine = ngDefine = (ngDefine || angularDefine(window.angular));
  }
})(window);