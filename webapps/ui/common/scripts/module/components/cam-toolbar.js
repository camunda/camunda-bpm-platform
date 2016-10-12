'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-toolbar.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    scope: {
      providers: '=camToolbar',
      vars: '=?'
    }
  };
};
