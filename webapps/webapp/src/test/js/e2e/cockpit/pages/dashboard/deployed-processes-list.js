'use strict';

var Base = require('./deployed-processes-plugin');

module.exports = Base.extend({

  tabLabel: 'List',

  listObject: function() {
    return this.pluginObject().element(by.css('.process-definitions-list'));
  },

  processesList: function() {
    return this.listObject().all(by.repeater('statistic in statistics'));
  },

  selectProcess: function(item) {
    return this.processesList().get(item).element(by.binding('{{ statistic.definition.name }}')).click();
  },

  processName: function(item) {
    return this.processesList().get(item).element(by.binding('{{ statistic.definition.name }}')).getText();
  },

  runningInstances: function(item) {
    return this.processesList().get(item).element(by.binding('{{ statistic.instances }}')).getText();
  }

});
