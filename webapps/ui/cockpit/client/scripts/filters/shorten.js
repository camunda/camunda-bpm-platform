  'use strict';
  var ShortenFilter = function() {
    return function(input, length) {
      return input.length > length ? input.substring(0, length) + '...' : input;
    };
  };
  module.exports = ShortenFilter;
