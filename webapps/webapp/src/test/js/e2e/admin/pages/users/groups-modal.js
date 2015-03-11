'use strict';

var Page = require('./edit-groups');

module.exports = Page.extend({

  pageHeader: function() {
    return element(by.css('.modal-header')).getText();
  },

  groupList: function() {
    return element.all(by.repeater('group in availableGroups'));
  },

  selectGroup: function(item) {
    this.groupList().get(item).element(by.model('group.checked')).click();
  },

  groupId: function(item) {
    return this.groupList().get(item).element(by.binding('{{group.id}}'));
  },

  groupName: function(item) {
    return this.groupList().get(item).element(by.binding('{{group.name}}'));
  },

  addSelectedGroupButton: function() {
    return element(by.css('[ng-click="createGroupMemberships()"]'));
  },

  cancelButton: function() {
    return element(by.css('[ng-click="close()"]'));
  },

  okButton: function() {
    return element(by.css('[ng-click="close(status)"]'));
  },

  addGroup: function(item) {
    var that = this;
    this.selectGroup(item);
    this.addSelectedGroupButton().click().then(function() {
      that.okButton().click();
    });
  }

});
