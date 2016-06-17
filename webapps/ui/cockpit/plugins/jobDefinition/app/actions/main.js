'use strict';

var angular = require('angular'),

    // override job priority action
    overrideJobPriorityAction = require('./override-job-priority/override-job-priority-action'),
    overrideJobPriorityDialog = require('./override-job-priority/override-job-priority-dialog'),

    // bulk override job priority action
    bulkOverrideJobPriorityAction = require('./bulk-override-job-priority/bulk-override-job-priority-action'),
    bulkOverrideJobPriorityDialog = require('./bulk-override-job-priority/bulk-override-job-priority-dialog');

var ngModule = angular.module('cockpit.plugin.jobDefinition.actions', []);

  // override job priority action
ngModule.config(overrideJobPriorityAction);
ngModule.controller('JobDefinitionOverrideJobPriorityController', overrideJobPriorityDialog);

  // bulk override job priority action
ngModule.config(bulkOverrideJobPriorityAction);
ngModule.controller('BulkJobDefinitionOverrideJobPriorityController', bulkOverrideJobPriorityDialog);

module.exports = ngModule;
