'use strict';

module.exports = [
  'search',
  function(search) {
    return function() {
      var lastSearchQuery = null;

      return function() {
        var searchQuery = search().searchQuery;

        if (searchQuery !== lastSearchQuery && (lastSearchQuery || searchQuery !== '[]')) {
          lastSearchQuery = searchQuery;

          return true;
        }
      };
    };
  }
];
