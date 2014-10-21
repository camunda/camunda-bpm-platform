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

  commentModalFormElement: function() {
    return element(by.name('newComment'));
  },

  addCommentButton: function() {
    return element(by.css('[ng-click="createComment()"]'));
  },

  commentInputField: function(inputValue) {
      var inputField = this.commentModalFormElement().element(by.model('comment.message'));

      if (arguments.length !== 0)
        inputField.sendKeys(inputValue);

      return inputField;
  },

  commentSaveButton: function() {
    return this.commentModalFormElement().element(by.css('[ng-click="submit()"]'));
  },

  addComment: function(comment) {
    this.addCommentButton().click();
    this.commentInputField(comment);
    this.commentSaveButton().click();
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
  },

  tabs: function() {
    return element.all(by.repeater('taskDetailTab in taskDetailTabs'));
  },

  selectTab: function(item) {

    this.tabs().get(item).click();
  }

});