'use strict';

var Base = require('./../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 3,
  tableRepeater: 'userTask in userTasks',

  userTaskName: function(item) {
    return this.tableItem(item, 'incident.rootCauseIncidentMessage').getText();
  },

  selectUserTask: function(item) {
    this.tableItem(item, 'incident.rootCauseIncidentMessage').click();
  },

  incidentActivity: function(item) {
    return this.tableItem(item, 'incident.activityName').getText();
  },

  selectIncidentActivity: function(item) {
    this.tableItem(item, 'incident.activityName').click();
  }

});