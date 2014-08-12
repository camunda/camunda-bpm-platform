'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({


  taskName: function() {
    return element(by.binding('task.name')).getText();
  },

  processName: function() {
    return element(by.binding('task._embedded.processDefinition.name')).getText();
  },

  completeButton: function() {
    return element(by.css('[ng-click="completeTask()"]'));
  }

});