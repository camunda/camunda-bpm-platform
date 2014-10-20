'use strict';

var Table = require('./../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 1,
  tableRepeater: 'calledProcessDefinition in calledProcessDefinitions',

  selectCalledProcessDefinitions: function(item) {
    this.tableItem(item, 'calledProcessDefinition.name').click();
  },

  calledProcessDefintionName: function(item) {
    return this.tableItem(item, 'calledProcessDefinition.name').getText();
  },

  selectCalledFromActivity: function(item) {
    this.tableItem(item, 'calledProcessDefinition.calledFromActivities[0].name').click()
  },

  calledFromActivityName: function(item) {
    return this.tableItem(item, 'activity.name').getText();
  }


});
