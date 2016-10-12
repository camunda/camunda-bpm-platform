'use strict';

module.exports = function() {
  return function($scope, target, properties) {
    var descriptors = properties.reduce(function(descriptors, property) {
      descriptors[property] = {
        get: function() {
          return $scope[property];
        },
        set: function(value) {
          $scope[property] = value;
        }
      };

      return descriptors;
    }, {});

    Object.defineProperties(target, descriptors);
  };
};
