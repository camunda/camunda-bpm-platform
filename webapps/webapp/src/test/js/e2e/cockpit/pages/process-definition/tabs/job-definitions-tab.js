'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 2,
  tabLabel: 'Job Definitions',
  tableRepeater: 'jobDefinition in jobDefinitions',

  state: function(idx) {
    return this.tableItem(idx, '.state:not(.ng-hide)');
  },

  activity: function(idx) {
    return this.tableItem(idx, '.activity');
  },

  configuration: function(idx) {
    return this.tableItem(idx, '.configuration');
  },

  suspendJobDefinitionButton: function(idx) {
    return this.tableItem(idx, '[ng-click="openSuspensionStateDialog(jobDefinition)"]:not(.ng-hide)');
  },

  activateJobDefinitionButton: function(idx) {
    return this.suspendJobDefinitionButton(idx);
  },

  suspendJobDefinition: function(idx) {
    var modal = this.modal;

    this.suspendJobDefinitionButton(idx).click().then(function() {
      browser.sleep(500);
      modal.suspendButton().click().then(function(){
        browser.sleep(500);
        modal.okButton().click();
      });
    });
  },

  activateJobDefinition: function(idx) {
    this.suspendJobDefinition(idx);
  }

});
