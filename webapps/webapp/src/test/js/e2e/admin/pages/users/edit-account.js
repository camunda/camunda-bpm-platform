'use strict';

var Page = require('./edit-base');

var changePasswordFormElement = element(by.css('form[name="updateCredentialsForm"]'));
var deleteUserFormElement = element(by.css('[ng-show="availableOperations.delete"]'));

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/users/:user?tab=account',

  subHeaderChangePassword: function() {
    return changePasswordFormElement.element(by.css('legend')).getText();
  },

  subHeaderDeleteUser: function() {
    return deleteUserFormElement.element(by.css('legend')).getText();
  },

  myPassword: function() {
    return changePasswordFormElement.element(by.model('credentials.authenticatedUserPassword'));
  },

  newPassword: function() {
    return changePasswordFormElement.element(by.model('credentials.password'));
  },

  newPasswordRepeat: function() {
    return changePasswordFormElement.element(by.model('credentials.password2'));
  },

  changePasswordButton: function() {
    return changePasswordFormElement.element(by.css('[data-ng-click="updateCredentials()"]'));
  },

  changePassword: function(myPassword, newPassword, newPasswordRepeat) {
    this.myPassword().sendKeys(myPassword);
    this.newPassword().sendKeys(newPassword);
    this.newPasswordRepeat().sendKeys(newPasswordRepeat);
    this.changePasswordButton().click();
  },

  deleteUserButton: function() {
    return deleteUserFormElement.element(by.css('[data-ng-click="deleteUser()"]'));
  },

  deleteUserAlert: function() {
    var ptor = protractor.getInstance();
    return ptor.switchTo().alert();
  },

  deleteUser: function() {
    this.deleteUserButton().click();
    this.deleteUserAlert().accept();
  }

});
