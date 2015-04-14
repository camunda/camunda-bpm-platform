'use strict';

var Page = require('./../base');

var groupsSection = element(by.id('groups'));

module.exports = Page.extend({

   selectAuthorizationNavbarItem: function(navbarItem) {
    var index = [
      'Application',
      'Authorization',
      'Filter',
      'Group',
      'Group Membership',
      'Process Definition',
      'Process Instance',
      'Task',
      'User'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 2;

    if (itemIndex)
      item = groupsSection.element(by.css('.sidebar-nav ul li:nth-child(' + itemIndex + ')'));
    else
      item = groupsSection.element(by.css('.sidebar-nav ul li:nth-child(1)'));

    item.click();
    return item;
  },

  boxHeader: function() {
    return groupsSection.element(by.css('[ng-controller="AuthorizationCreateController"] legend')).getText();
  },

  createNewButton: function() {
    return element(by.css('[ng-hide="isCreateNewAuthorization"]')).element(by.css('[ng-click="toggleCreateNewForm()"]'));
  },

  createNewElement: function() {
    return element(by.id('createNew'));
  },

  authorizationType: function(authType) {
    element(by.cssContainingText('option', authType.toUpperCase())).click();
  },

  identityIdButton: function() {

  },

  identityIdInputFiled: function(inputValue) {
    var inputField = this.createNewElement().element(by.model('newAuthorization.identityId'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  permissions: function() {

  },

  resourceId: function(inputValue) {
    var inputField = this.createNewElement().element(by.model('newAuthorization.resourceId'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  submitNewAuthorizationButton: function() {
    return this.createNewElement().element(by.css('[ng-click="createAuthorization()"]'));
  },

  abortNewAuthorizationButton: function() {
    return this.createNewElement().element(by.css('[ng-click="toggleCreateNewForm()"]'));
  }

});
