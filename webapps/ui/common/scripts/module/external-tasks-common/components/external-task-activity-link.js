'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/external-task-activity-link.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    controller: 'ExternalTaskActivityLinkController as Link',
    scope: {
      activityId: '=externalTaskActivityLink',
      bpmnElements: '=',
      searchQueryType: '=?'
    }
  };
};
