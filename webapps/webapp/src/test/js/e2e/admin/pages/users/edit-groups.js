'use strict';

var Page = require('./edit-base');

var formElement = element(by.css('form[name="updateGroupMemberships"]'));

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/users/:user?tab=groups',

  subHeader: function() {
    return formElement.element(by.css('legend')).getText();
  },

  groupList: function() {
    return formElement.all(by.repeater('group in groupList'));
  },

  groupName: function(item) {
    return this.groupList().get(item).element(by.binding('{{group.id}}')).getText();
  },

  addGroupButton: function() {
    return formElement.element(by.css('[ng-click="openCreateGroupMembershipDialog()"]'));
  },

  removeGroup: function(item) {
    this.groupList().get(item).element(by.css('[ng-click="removeGroup(group.id)"]')).click();
  }
});
