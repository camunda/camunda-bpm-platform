'use strict';

module.exports = function(obj, includePrototype) {
  var keys = [];
  var key;

  for (key in obj) {
    if (includePrototype || obj.hasOwnProperty(key)) {
      keys.push(key);
    }
  }

  return keys;
};
