'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$q',
  function($q) {
    return function(getCount, getList) {
      return function(query, pages) {
        return getCount(query)
          .then(function(data) {
            var first = (pages.current - 1) * pages.size;
            var count = data.count;
            var listQuery = angular.extend(
              {},
              query,
              {
                firstResult: first,
                maxResults: pages.size
              }
            );

            if (count > first) {
              return $q.all({
                count: count,
                list: getList(listQuery)
              });
            }

            return data;
          });
      };
    };
  }
];
