'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-task]'));
  },

  noTaskInfoText: function() {
    return this.formElement().element(by.css('.no-task')).getText();
  },

  waitForTaskDetailView: function() {
    var elementToWaitFor = this.taskName();
    this.waitForElementToBeVisible(elementToWaitFor);
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
    var openDialogElement = this.commentInputField();
    this.addCommentButton().click();
    this.waitForElementToBeVisible(openDialogElement);

    this.commentInputField(comment);

    var closedDialogElement = this.commentSaveButton();
    this.commentSaveButton().click();
    this.waitForElementToBeNotPresent(closedDialogElement);
  },

  claim: function() {
    var claimButton = element(by.css('[ng-click="claim()"]'));
    claimButton.click();
    this.waitForElementToBeNotPresent(claimButton);
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
    element(by.css('.set-value [ng-click*="startEditing()"]')).click();
  },

  editClaimedUser: function(userName) {
    this.clickClaimedUserField();
    this.claimedUserFieldEditMode().clear();
    this.claimedUserFieldEditMode().sendKeys(userName);
    element(by.css('body > .cam-widget-inline-field > [ng-click="applyChange($event);"]')).click();
  },

  cancelEditClaimedUser: function() {
    element(by.css('[ng-click*="cancelChange($event)"]')).click();
  },

  datePickerDialogElement: function() {
    return element(by.css('.cam-widget-inline-field'));
  },

  editDate: function(newTime, newDate) {
    var datePickerButton = element(by.css('.cam-widget-inline-field.btn-group'));
    var datePickerField = element(by.css('.cam-widget-inline-field.field-control'));
    this.waitForElementToBeVisible(datePickerField);

    if (newTime) {
      var timeValue = newTime.split(':')
      var timePickerElement = datePickerField.element(by.css('.timepicker'));

      timePickerElement.element(by.model('hours')).clear().sendKeys(timeValue[0]);
      timePickerElement.element(by.model('minutes')).clear().sendKeys(timeValue[1]);

      // ToDo: implement date editing
    };

    datePickerButton.element(by.css('[ng-click*="applyChange($event)"]')).click();
    this.waitForElementToBeNotPresent(datePickerField);
  },

  followUpDateElement: function() {
    return this.formElement().element(by.css('.followup-date'));
  },

  setFollowUpDate: function(newTime, newDate) {
    this.followUpDateElement().element(by.css('[ng-click*="startEditing()"]')).click();
    this.editDate(newTime, newDate);
    browser.sleep(500);
  },

  followUpDate: function() {
    return this.followUpDateElement().element(by.css('.view-value')).getText();
  },

  followUpDateTooltip: function() {
    var tooltipTriggerer = this.followUpDateElement().element(by.css('[am-time-ago="task.followUp"]'))
    browser.actions().mouseMove(tooltipTriggerer).perform();

    var tooltipWidget = element(by.css('body > [tooltip-popup]'));
    this.waitForElementToBeVisible(tooltipWidget);
    return tooltipWidget.getText();
  },

  dueDateElement: function() {
    return this.formElement().element(by.css('.due-date'));
  },

  setDueDate: function(newTime, newDate) {
    this.dueDateElement().element(by.css('[ng-click*="startEditing()"]')).click();
    this.editDate(newTime, newDate);
    browser.sleep(500);
  },

  dueDate: function() {
    return this.dueDateElement().element(by.css('.view-value')).getText();
  },

  dueDateTooltip: function() {
    var tooltipTriggerer = this.dueDateElement().element(by.css('[am-time-ago="task.due"]'))
    browser.actions().mouseMove(tooltipTriggerer).perform();

    var tooltipWidget = element(by.css('body > [tooltip-popup]'));
    this.waitForElementToBeVisible(tooltipWidget);
    return tooltipWidget.getText();
  },

  taskTenantIdField: function() {
    return this.formElement().element(by.css('.tenant-id'));
  }

});
