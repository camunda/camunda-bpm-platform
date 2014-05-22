'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
define(function() {
  var _cache = {};
  var camAPI = {};

  camAPI.register = function(config) {
    config = config || {};

    if (!config.url) {
      throw new Error('the configuration needs a URL.');
    }

    if (!config.name) {
      throw new Error('the configuration needs a name.');
    }

    if (!!_cache[config.name]) {
      throw new Error('the resource "'+ config.name +'" is already registered.');
    }

    var constructor = _cache[config.name] = function(options) {
      this.options = options || {};
    };

    // Model methods
    constructor.query = function() {

    };
    constructor.create = function() {

    };

    // Model instance methods
    constructor.prototype.fetch = function() {

    };
    constructor.prototype.save = function() {

    };
    constructor.prototype.delete = function() {

    };

    return constructor;
  };

  return camAPI;
});
