'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  taskList: function() {
    return element.all(by.repeater('(delta, task) in tasks'));
  },

  taskTitle: function(item) {
    return this.taskList().get(item).element(by.binding('task._embedded.processDefinition.name')).getText();
  },

  selectTask: function(item) {
    this.taskList().get(item).element(by.binding('task._embedded.processDefinition.name')).click();
  }

});