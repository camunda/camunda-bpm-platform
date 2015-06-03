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
    var claimButton = element(by.css('[ng-click="claim()"]'));
    claimButton.click();
    this.waitForElementToBeNotPresent(claimButton, 5000);
  },

  unclaim: function() {
    element(by.css('[ng-click="unclaim()"]')).click();
  },

  isTaskClaimed: function() {
    return element(by.css('.task-card .assignee[ng-if="task.assignee"]')).isPresent();
  },

  claimedUserField: function() {
    return element(by.css('.task-card .assignee'));
  },

  claimedUserFieldEditMode: function() {
    return this.claimedUserField().element(by.model('editValue'));
  },

  claimedUser: function() {
    return this.claimedUserField().getText();
  },

  clickClaimedUserField: function() {
    element(by.css('[class="set-value ng-isolate-scope"] [ng-click="startEditing()"]')).click();
  },

  editClaimedUser: function(userName) {
    this.clickClaimedUserField();
    this.claimedUserFieldEditMode().clear();
    this.claimedUserFieldEditMode().sendKeys(userName);
    element(by.css('[ng-click="applyChange($event)"]')).click();
  },

  cancelEditClaimedUser: function() {
    element(by.css('[ng-click="cancelChange($event)"]')).click();
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
