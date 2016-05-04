'use strict';

var Page = require('./../base');

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/groups/:group?tab=group',

  updateGroupButton: function() {
    return element(by.css('[ng-click="updateGroup()"]'));
  },

  groupNameInput: function(inputValue) {
    var inputField = element(by.model('group.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  groupTypeInput: function(inputValue) {
    var inputField = element(by.model('group.type'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  deleteGroupButton: function() {
    return element(by.css('[ng-click="deleteGroup()"]'));
  },

  deleteGroupAlert: function() {
    return browser.switchTo().alert();
  },

  deleteGroup: function() {
    this.deleteGroupButton().click();
    element(by.css('.modal-footer [ng-click="$close()"]')).click();
  },

  selectUserNavbarItem: function(navbarItem) {
    var index = [
      'Group',
      'Tenants',
      'Users'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (itemIndex) {
      item = element(by.css('aside ul li:nth-child(' + itemIndex + ')'));
    }
    else {
      item = element(by.css('aside ul li:nth-child(1)'));
    }

    item.click();
    return item;
  }

});
