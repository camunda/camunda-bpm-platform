'use strict';

module.exports = function() {
  return function(object, path, defaultValue) {
    var current = object;
    var property;

    for (var i = 0; i < path.length; i++) {
      property = path[i];

      if (current && current[property]) {
        current = current[property];
      } else {
        return defaultValue;
      }
    }

    return current;
  };
};
