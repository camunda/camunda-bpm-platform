define([
  'angular'
], function(
  angular
) {
  'use strict';
  return function() {
    return function(input) {
      return input ? input : '??';
    };
  };
});
