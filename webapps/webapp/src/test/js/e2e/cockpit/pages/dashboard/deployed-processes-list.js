'use strict';

var Base = require('./dashboard-view');

var tableForm = element(by.css('.table'));

module.exports = Base.extend({

  processesList: function () {
    return tableForm.all(by.repeater('statistic in statistics'));
  },

  selectProcess: function(item) {
    return this.processesList().get(item).element(by.binding('definition.name')).click();
  },

  processName: function(item) {
    return this.processesList().get(item).element(by.binding('definition.name')).getText();
  },

  runningInstances: function(item) {
    return this.processesList().get(item).element(by.binding('.instances')).getText();
  }

});