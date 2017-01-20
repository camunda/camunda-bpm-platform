'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-search.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    controller: 'CamSearchController as CamSearch',
    scope: {
      config: '=camSearch',
      arrayTypes: '=?',
      variableTypes: '=?',
      storageGroup: '=?',
      onQueryChange: '&'
    }
  };
};
