'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-filter-tasks]'));
  },

  taskList: function() {
    return this.formElement().all(by.repeater('(delta, task) in tasks'));
  },

  selectTask: function(item) {
    this.taskList().get(item).element(by.binding('task.name')).click();
  },

  taskName: function(item) {
    return this.taskList().get(item).element(by.css('[class="task"]')).element(by.binding('task.name')).getText();
  },

  taskProcessDefinitionName: function(item) {
    return this.taskList().get(item).element(by.binding('task._embedded.processDefinition[0].name')).getText();
  }

});