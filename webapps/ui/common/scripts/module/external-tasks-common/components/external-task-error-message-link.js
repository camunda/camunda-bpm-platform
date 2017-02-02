'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/external-task-error-message-link.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    transclude: true,
    controller: 'ExternalTaskErrorMessageLinkController as Link',
    scope: {
      taskId: '=externalTaskErrorMessageLink',
      historic: '=?'
    }
  };
};
