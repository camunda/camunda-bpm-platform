'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-task]'));
  },

  taskName: function() {
    return element(by.binding('task.name')).getText();
  },

  processName: function() {
    return element(by.binding('task._embedded.processDefinition.name')).getText();
  },

  completeButton: function() {
    return element(by.css('[ng-click="completeTask()"]'));
  },

  claim: function() {
    element(by.css('[ng-click="claim()"]')).click();
  },

  unclaim: function() {
    element(by.css('[ng-click="unclaim()"]')).click();
  }

});