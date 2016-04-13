'use strict';

var Base = require('./deployed-processes-plugin');

module.exports = Base.extend({

  tabLabel: 'List',

  listObject: function() {
    return element(by.css('.process-definitions-list'));
  },

  processesList: function() {
    return this.listObject().all(by.css('tbody tr'));
  },

  selectProcess: function(item) {
    return this.processesList().get(item).element(by.css('.name a')).click();
  },

  selectProcessByName: function(name) {
    return this.listObject().element(by.cssContainingText('tbody tr .name a', name)).click();
  },

  processName: function(item) {
    return this.processesList().get(item).element(by.css('.name a')).getText();
  },

  runningInstances: function(item) {
    return this.processesList().get(item).element(by.binding('{{ pd.instances }}')).getText();
  },

  tenantId: function(item) {
    return this.processesList().get(item).element(by.css('.tenant-id')).getText();
  },

  getReportColumn: function() {
    return this.pluginObject().element(by.css('th.report-link'));
  }

});
