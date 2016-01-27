'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 1,
  tabLabel: 'Incidents',
  tableRepeater: 'incident in incidents',

  incidentMessage: function(item) {
    return this.tableItem(item, '.message');
  },

  incidentActivity: function(item) {
    return this.tableItem(item, '.activity');
  }

});
