'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define(['angular'], function(angular) {

  /**
   * Utilities module
   * @memberof cam.tasklist
   */

  /**
   * @type {angularModule}
   */
  var utilsModule = angular.module('cam.tasklist.utils', []);


  /**
   * Provides a method to get globally unique IDs
   * @return {Integer} a globally unique integer
   */
  utilsModule.factory('camUID', function() {
    var _counter = 0;
    return function(prefix) {
      _counter++;
      return _counter;
    };
  });


  var CamStorage = function(options) {
    options = options || {};

    this.prefix = options.prefix || '';
    this.storage = localStorage;
  };

  CamStorage.prototype.set = function(key, value) {
    this.storage.setItem(this.prefix + key, JSON.stringify(value));
    return this;
  };

  CamStorage.prototype.get = function(key) {
    return JSON.parse(this.storage.getItem(this.prefix + key));
  };

  CamStorage.prototype.has = function(key) {
    return !!this.storage[this.prefix + key];
  };

  CamStorage.prototype.remove = function(key) {
    this.storage.removeItem(this.prefix + key);
    return this;
  };

  utilsModule.factory('camStorage', function() {
    return new CamStorage();
  });

  utilsModule.factory('camSettings', function() {
    var settings = window.tasklistConf || {};
    return function(ngModule) {
      return settings[ngModule.name ? ngModule.name : ngModule] || {};
    };
  });

  return utilsModule;
});
