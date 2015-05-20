'use strict';

var Page = require('./../base');

var groupsSection = element(by.css('section.authorizations'));

module.exports = Page.extend({

   selectAuthorizationNavbarItem: function(navbarItem) {
    var index = [
      'Application',
      'Authorization',
      'Deployment',
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
    return element(by.css('[ng-hide="isCreateNewAuthorization"]')).element(by.css('[ng-click="addNewAuthorization()"]'));
  },

  authorizationList: function() {
    return element.all(by.repeater('authorization in authorizations'));
  },

  createNewElement: function() {
    return this.authorizationList().last();
  },

  authorizationType: function(authType) {
    return element.all(by.css('.authorization-type')).last().element(by.cssContainingText('option', authType.toUpperCase()));
  },

  identityButton: function() {
    return this.createNewElement().element(by.css('.input-group-addon'));
  },

  isIdentityButtonGroup: function() {
    return this.identityButton().getAttribute('tooltip')
      .then(function(classes) {
        return classes.indexOf('Group') !== -1;
      });
  },

  identityIdInputFiled: function(inputValue) {
    var inputField = this.createNewElement().element(by.model('authorization.identityId'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  permissionsField: function() {
    return this.createNewElement().element(by.css('.input-group .form-control-static'));
  },

  permissionsButton: function() {
    return this.createNewElement().element(by.css('.input-group button'));
  },

  permissionsDropdownList: function() {
    return this.createNewElement().all(by.repeater('perm in availablePermissions'));
  },

  selectPermission: function(index, permissionsType) {
    this.permissionsButton().click();

    if (arguments.length === 1 && typeof index === 'string') {
      var that = this;
      permissionsType = index;

      this.findElementIndexInRepeater('perm in availablePermissions', by.css('a'), permissionsType)
        .then(function(idx) {
          that.permissionsDropdownList().get(idx).click();
        });
    } else {
      this.permissionsDropdownList().get(index).click();
    };
  },

  permissionsDropdownElements: function(index) {
    return permissionsDropdownList().get(index);
  },

  resourceIdField: function(inputValue) {
    var inputField = this.createNewElement().element(by.model('authorization.resourceId'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  submitNewAuthorizationButton: function() {
    return this.createNewElement().element(by.css('[ng-click="confirmUpdateAuthorization(authorization)"]'));
  },

  abortNewAuthorizationButton: function() {
    return this.createNewElement().element(by.css('[ng-click="cancelUpdateAuthorization(authorization)"]'));
  },

  createNewAuthorization: function(authType, identityType, identityId, permissions, resourceId) {
    var that = this;

    this.createNewButton().click();
    this.authorizationType(authType).click();
    this.isIdentityButtonGroup().then(function(state) {
      if (state && (identityType.toUpperCase() === 'USER')) {
        that.identityButton().click();
      } else if (!state && (identityType.toUpperCase() === 'GROUP')){
        that.identityButton().click();
      }
    });
    this.identityIdInputFiled().clear();
    this.identityIdInputFiled(identityId);
    this.selectPermission(permissions);
    this.resourceIdField().clear();
    this.resourceIdField(resourceId);
    this.submitNewAuthorizationButton().click();
  }

});
