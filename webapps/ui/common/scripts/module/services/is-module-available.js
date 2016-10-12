'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = function() {
  return function(moduleName) {
    try {
      return !!angular.module(moduleName);
    } catch(err) {
      return false;
    }
  };
};
