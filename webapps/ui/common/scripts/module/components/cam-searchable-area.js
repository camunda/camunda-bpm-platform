'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-searchable-area.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    transclude: true,
    template: template,
    controller: 'CamSearchAbleAreaController as Searchable',
    scope: {
      config: '=',
      total: '=',
      loadingState: '=',
      loadingError: '=',
      arrayTypes: '=?',
      variableTypes: '=?',
      onSearchChange: '&',
      textEmpty: '@'
    }
  };
};
