'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasks]'));
  },

  taskList: function() {
    return this.formElement().all(by.repeater('(delta, task) in tasks'));
  },

  taskListInfoText: function() {
    return this.formElement().getText();
  },

  getTaskIndex: function(taskName) {
    return this.findElementIndexInRepeater('(delta, task) in tasks', by.css('.names .task'), taskName).then(function(idx) {
      return idx;
    });
  },

  selectTask: function(idxOrName) {
    function callPageObject(idx) {
      this.taskList().get(idx).element(by.binding('task.name')).click();
      this.waitForElementToBeVisible(element(by.css('.task-details .names .task')));
    }

    if (typeof idxOrName === 'number') {
      callPageObject.call(this, idxOrName);
    } else {
      this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
    }
  },

  taskName: function(idx) {
    return this.taskList().get(idx).element(by.binding('task.name')).getText();
  },

  taskProcessDefinitionName: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskList().get(idx).element(by.binding('task._embedded.processDefinition[0].name')).getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskPriority: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskList().get(idx).element(by.binding('task.priority')).getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskCreated: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskList().get(idx).element(by.binding('task.created')).getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskFollowUpDateElement: function(idx) {
    var theElement = this.taskList().get(idx).element(by.css('.followup-date'));
    this.waitForElementToBeVisible(theElement);
    return theElement;
  },

  taskFollowUpDate: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskFollowUpDateElement(idx).getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskFollowUpDateTooltip: function(idxOrName) {
    function callPageObject(idx) {
      var tooltipTriggerer = this.taskFollowUpDateElement(idx)
                              .element(by.css('[am-time-ago="task.followUp"]'));
      browser.actions().mouseMove(tooltipTriggerer).perform();

      var tooltipWidget = element(by.css('body > [tooltip-popup]'));
      this.waitForElementToBeVisible(tooltipWidget);
      return tooltipWidget.getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskDueDateElement: function(idx) {
    var theElement = this.taskList().get(idx).element(by.css('.due-date'));
    this.waitForElementToBeVisible(theElement);
    return theElement;
  },

  taskDueDate: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskDueDateElement(idx).getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.apply(this,idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskDueDateTooltip: function(idxOrName) {
    function callPageObject(idx) {
      var tooltipTriggerer = this.taskDueDateElement(idx)
                              .element(by.css('[am-time-ago="task.due"]'));
      browser.actions().mouseMove(tooltipTriggerer).perform();

      var tooltipWidget = element(by.css('body > [tooltip-popup]'));
      this.waitForElementToBeVisible(tooltipWidget);
      return tooltipWidget.getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskAssigneeField: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskList().get(idx).element(by.css('.assignee'));
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskAssignee: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskAssigneeField(idx).getText();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskVariables: function(idxOrName) {
    function callPageObject(idx) {
      return this.taskList().get(idx).all(by.repeater('(delta, info) in variableDefinitions'));
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  showMoreVariables: function(idxOrName) {
    function callPageObject(idx) {
      this.taskList().get(idx).element(by.css('.shutter .glyphicon-menu-down')).click();
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName);
    }

    return this.getTaskIndex(idxOrName).then(callPageObject.bind(this));
  },

  taskVariableNameElement: function(idx, variableIdx) {
    browser.actions().mouseMove(this.taskVariables(idx).get(variableIdx).element(by.css('.variable-label'))).perform();

    browser.sleep(1000);

    return element(by.css('.tooltip'));
  },

  taskVariableName: function(idxOrName, variableIdx) {
    function callPageObject(idx, varIdx) {
      return this.taskVariableNameElement(idx, varIdx);
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName, variableIdx);
    }

    return this.getTaskIndex(idxOrName, variableIdx).then(callPageObject.bind(this));
  },

  taskVariableLabel: function(idxOrName, variableIdx) {
    function callPageObject(idx, varIdx) {
      return this.taskVariables(idx).get(varIdx).element(by.css('.variable-label'));
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName, variableIdx);
    }

    return this.getTaskIndex(idxOrName, variableIdx).then(callPageObject.bind(this));
  },

  taskVariableValue: function(idxOrName, variableIdx) {
    function callPageObject(idx, varIdx) {
      return this.taskVariables(idx).get(varIdx).element(by.css('.variable-value'));
    }

    if (typeof idxOrName === 'number') {
      return callPageObject.call(this, idxOrName, variableIdx);
    }

    return this.getTaskIndex(idxOrName, variableIdx).then(callPageObject.bind(this));
  }

});
