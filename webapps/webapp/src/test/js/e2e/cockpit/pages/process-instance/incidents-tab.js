'use strict';

var Base = require('./../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 1,
  tableRepeater: 'incident in incidents',

  incidentMessage: function(item) {
    return this.tableItem(item, 'incident.rootCauseIncidentMessage').getText();
  },

  selectIncidentMessage: function(item) {
    this.tableItem(item, 'incident.rootCauseIncidentMessage').click();
  },

  incidentActivity: function(item) {
    return this.tableItem(item, 'incident.activityName').getText();
  },

  selectIncidentActivity: function(item) {
    this.tableItem(item, 'incident.activityName').click();
  }

});