'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 1,
  tabLabel: 'Called Process Definitions',
  tableRepeater: 'calledProcessDefinition in calledProcessDefinitions',

  calledProcessDefintion: function(item) {
    return this.tableItem(item, '.process-definition');
  },

  activity: function(item) {
    return this.tableItem(item, '.activity');
  }

});
