'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decisions-table.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    scope: {
      decisionCount: '=',
      decisions: '=',
      isDrdAvailable: '='
    }
  };
};
