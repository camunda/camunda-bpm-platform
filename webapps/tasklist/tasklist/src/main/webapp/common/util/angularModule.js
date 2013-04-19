define([ "angular", "angularReady" ], function(angular, angularReady) {

  var each = angular.forEach;

  var isModule = function(module) {
    return module.indexOf(".") != -1;
  };

  var asFileName = function(module) {
    return module.replace(/\./g, "/");
  };

  var extractModuleName = function(file) {
    var idx = file.lastIndexOf("/");

    if (idx == -1) {
      return file;
    } else {
      return file.substring(0, idx).replace(/\//g, ".");
    }
  };

  var INTERNAL = /^ng/;
  var REQUIRE_EVENT = /^(.*!)$/;

  var angularModule = function(name, dependencies) {
    var deps = dependencies || [],
        modules = [],
        files = [];

    each(deps, function(e) {
      var module, file;

      if (isModule(e) || INTERNAL.test(e)) {
        file = asFileName(e);
        module = e;

        if (modules.indexOf(module) == -1) {
          modules.push(module);
        }
      } else {
        file = e;
      }

      var internal = file && INTERNAL.test(file);
      
      if (!internal && file) {
        files.push(file);
      }
    });

    angularReady.loading(name);

    // require files
    require(files, function() {
      console.log("[load module]", name, modules, files);
      angularReady.loaded(name);
    });

    return angular.module(name, modules);
  };

  return angularModule;
});