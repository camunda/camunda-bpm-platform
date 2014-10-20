'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-task]'));
  },

  taskName: function() {
    return this.formElement().element(by.binding('task.name')).getText();
  },

  processName: function() {
    return this.formElement().element(by.binding('task._embedded.processDefinition[0].name')).getText();
  },

  completeButton: function() {
    return element(by.css('[ng-click="completeTask()"]'));
  },

  claim: function() {
    element(by.css('[ng-click="claim()"]')).click();
  },

  unclaim: function() {
    element(by.css('[ng-click="unclaim()"]')).click();
  },

  editClaimedUser: function(userName) {
    element(by.css('[class="set-value ng-isolate-scope"] [ng-click="startEditing()"]')).click();
    element(by.model('editValue')).clear();
    element(by.model('editValue')).sendKeys(userName);
    element(by.css('[ng-click="applyChange()"]')).click();
  },

  setFollowUpDate: function(month, day, hour, minute) {
    element(by.css('[class="followup-date ng-isolate-scope"] [ng-click="startEditing()"]')).click();
  },

  setDueDate: function(month, day, hour, minute) {
    element(by.css('[class="due-date ng-isolate-scope"] [ng-click="startEditing()"]')).click();
  }


});