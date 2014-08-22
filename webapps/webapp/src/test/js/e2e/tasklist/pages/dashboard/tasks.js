'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-filter-tasks]'));
  },

  taskList: function() {
    return this.formElement().all(by.repeater('(delta, task) in tasks'));
  },

  taskTitle: function(item) {
    return this.taskList().get(item).element(by.css('[class="task"]')).element(by.binding('task.name')).getText();
  },

  taskProcessDefinitionName: function(item) {
    return this.taskList().get(item).element(by.binding('task._embedded.processDefinition.name')).getText();
  },

  selectTask: function(item) {
    this.taskList().get(item).element(by.binding('task.name')).click();
  },

  searchTaskInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('searchTask'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  foundTasksList: function() {
    return this.formElement().all(by.repeater('match in matches track by $index'));
  },

  selectTaskfromSearchResults: function(item) {
    this.foundTasksList().get(item).element(by.binding('task.name')).click();
  }

});