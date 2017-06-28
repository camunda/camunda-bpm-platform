'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-searchable-area.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    transclude: true,
    template: template,
    controller: 'CamPaginationSearchIntegrationController as Searchable',
    scope: {
      config: '=',
      loadingState: '=',
      loadingError: '=',
      arrayTypes: '=?',
      variableTypes: '=?',
      buildCustomQuery: '&',
      onSearchChange: '&',
      textEmpty: '@',
      storageGroup: '=',
      blocked: '='
    }
  };
};
