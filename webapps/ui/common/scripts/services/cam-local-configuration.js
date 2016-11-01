'use strict';
module.exports = ['$window', function($window) {
  var storage = $window.localStorage;
  var values = JSON.parse(storage.getItem('camunda') || '{}');
  return {
    get: function(key, defaultValue) {
      return typeof values[key] !== 'undefined' ? values[key] : defaultValue;
    },
    set: function(key, value) {
      values[key] = value;
      storage.setItem('camunda', JSON.stringify(values));
    },
    remove: function(key) {
      delete values[key];
      storage.setItem('camunda', JSON.stringify(values));
    }
  };
}];