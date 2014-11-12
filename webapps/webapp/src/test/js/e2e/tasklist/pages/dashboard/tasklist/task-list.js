'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasks]'));
  },

  taskList: function() {
    return this.formElement().all(by.repeater('(delta, task) in tasks'));
  },

  selectTask: function(item) {
    this.taskList().get(item).element(by.binding('task.name')).click();
  },

  taskName: function(item) {
    return this.taskList().get(item).element(by.binding('task.name')).getText();
  },

  taskProcessDefinitionName: function(item) {
    return this.taskList().get(item).element(by.binding('task._embedded.processDefinition[0].name')).getText();
  },

  taskPriority: function(item) {
    return this.taskList().get(item).element(by.binding('task.priority')).getText();
  },

  taskCreated: function(item) {
    return this.taskList().get(item).element(by.binding('task.created')).getText();
  },

  taskAssignee: function(item) {
    return this.taskList().get(item).element(by.binding('task.assignee')).getText();
  }

});