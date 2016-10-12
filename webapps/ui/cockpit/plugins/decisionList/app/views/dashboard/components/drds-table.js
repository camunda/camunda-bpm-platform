'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/drds-table.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    scope: {
      drds: '=',
      drdsCount: '=',
      isDrdAvailable: '='
    }
  };
};
