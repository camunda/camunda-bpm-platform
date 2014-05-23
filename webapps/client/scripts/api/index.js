'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
define(['jquery'], function($) {
  var _cache = {};

  function replaceVars(str, vars) {
    for (var v in vars) {
      str = str.replace(new RegExp('{{'+ v +'}}', 'g'), vars[v]);
    }
    return str;
  }

  var camAPI = function(name) {
    if (!_cache[name]) {
      throw new Error('the resource "'+ name +'" has not been registered yet');
    }

    // var method, options;

    // if (arguments.length === 3) {
    //   method = arguments[1];
    //   options = arguments[2];

    //   return _cache[config.name][method](options);
    // }
    // else if (arguments.length === 2) {

    // }


    return _cache[name];
  };

  camAPI.register = function(config) {
    config = config || {};

    if (!config.url) {
      throw new Error('the configuration needs a URL');
    }

    if (!config.name) {
      throw new Error('the configuration needs a name');
    }

    if (!!_cache[config.name]) {
      throw new Error('the resource "'+ config.name +'" is already registered');
    }

    var constructor = _cache[config.name] = function(options) {
      this.options = options || {};
    };

    // Model methods


    constructor.query = function() {

    };


    constructor.create = function() {

    };


    var modelMethods = config.modelMethods || {};
    var mm;
    for (mm in modelMethods) {
      if (modelMethods.hasOwnProperty(mm)) {
        constructor[mm] = modelMethods[mm];
      }
    }



    // Model instance methods


    constructor.prototype.fetch = function() {

    };


    constructor.prototype.save = function() {

    };


    constructor.prototype.delete = function() {

    };


    var instanceMethods = config.instanceMethods || {};
    var im;
    for (im in instanceMethods) {
      if (instanceMethods.hasOwnProperty(im)) {
        constructor.prototype[im] = instanceMethods[im];
      }
    }


    return constructor;
  };

  function makeLegacyMethod(action) {
    return function(options) {
      var deferred = this.defer();
      options = options || {};

      // var success = options.success;
      // var error = options.error;

      var reqParams = {
        type:         action.method || 'GET',
        dataType:     action.dataType || 'json',
        contentType:  action.contentType || 'application/json;charset=utf-8',
        url:          this.baseUrl + replaceVars(action.path, options.instance || {})
      };

      if (options.data) {
        reqParams.data = JSON.stringify(options.data);
      }

      deferred.notify('request:start');

      $.ajax(reqParams)
        .done(function(data) {
          deferred.resolve(data);
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
          deferred.reject(textStatus);
        })
        .always(function() {
          deferred.notify('request:complete');
        });

      return deferred.promise;
    };
  }

  camAPI.registerLegacy = function(config) {
    config = config || {};

    if (!config.defer) {
      throw new Error('the configuration needs a "defer" property');
    }

    if (!config.baseUrl) {
      throw new Error('the configuration needs a "baseUrl" property');
    }

    if (!config.name) {
      throw new Error('the configuration needs a "name" property');
    }

    if (!!_cache[config.name +'']) {
      throw new Error('the resource "'+ config.name +'' +'" is already registered');
    }

    var constructor = _cache[config.name +''] = function(options) {
      options = options || {};
      this.options = options;
      this.baseUrl = options.baseUrl || config.baseUrl;
      this.defer = options.defer || config.defer;
    };

    config.actions = config.actions || {};
    var action;
    for (var name in config.actions) {
      action = config.actions[name];
      constructor.prototype[name] = makeLegacyMethod(action);
    }


    return constructor;
  };

  return camAPI;
});
