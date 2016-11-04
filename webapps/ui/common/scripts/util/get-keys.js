'use strict';

module.exports = function(obj, includePrototype) {
  if (!includePrototype) {
    return Object.keys(obj);
  }

  return getKeysIncludingPrototype(obj);
};

function getKeysIncludingPrototype(obj) {
  var keys = [];
  var key;

  for (key in obj) {
    keys.push(key);
  }

  return keys;
}
