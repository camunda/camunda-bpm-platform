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

  claimedUserName: function() {
    return element(by.css('.task-card .assignee')).getText();
  },

  editClaimedUser: function(userName) {
    element(by.css('[class="set-value ng-isolate-scope"] [ng-click="startEditing()"]')).click();
    element(by.model('editValue')).clear();
    element(by.model('editValue')).sendKeys(userName);
    element(by.css('[ng-click="applyChange()"]')).click();
  },

  followUpDateFormElement: function() {
    return this.formElement().element(by.css('.followup-date'));
  },

  setFollowUpDate: function() {
    this.followUpDateFormElement().element(by.css('[ng-click="startEditing()"]')).click();
    this.followUpDateFormElement().element(by.css('[ng-click="applyChange()"]')).click();
  },

  followUpDateText: function() {
    return this.followUpDateFormElement().element(by.css('[class="view-value ng-scope"] span[ng-if="varValue"], [class="view-value ng-scope"] a')).getText();
  },

  dueDateFormElement: function() {
    return this.formElement().element(by.css('.due-date'));
  },

  setDueDate: function() {
    this.dueDateFormElement().element(by.css('[ng-click="startEditing()"]')).click();
    this.dueDateFormElement().element(by.css('[ng-click="applyChange()"]')).click();
  },

  dueDateText: function() {
    return this.dueDateFormElement().element(by.css('[class="view-value ng-scope"] span[ng-if="varValue"], [class="view-value ng-scope"] a')).getText();
  }

});
