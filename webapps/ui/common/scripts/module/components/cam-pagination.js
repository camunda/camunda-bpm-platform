'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-pagination.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    controller: 'CamPaginationController as Pagination',
    scope: {
      total: '=',
      onPaginationChange: '&camPagination'
    }
  };
};
