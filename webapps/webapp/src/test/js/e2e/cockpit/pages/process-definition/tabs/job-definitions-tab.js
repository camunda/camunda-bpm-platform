'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 2,
  tabLabel: 'Job Definitions',
  tableRepeater: 'jobDefinition in jobDefinitions',

  selectJobDefinition: function(item) {
    this.tableItem(item, 'jobDefinition.activityName').click();
  },

  jobDefinitionName: function(item) {
    return this.tableItem(item, 'jobDefinition.activityName').getText();
  },

  suspendJobButton: function(item) {
    //return this.table().get(item).element(by.css('[ng-click="openSuspensionStateDialog(jobDefinition)"]:visible'));
    return this.table().get(item).element(by.css('.btn.action-button[tooltip="Suspend Job Definition"]'));
  },

  suspendJob: function(item) {
    this.suspendJobButton(item).click();
  }

});
