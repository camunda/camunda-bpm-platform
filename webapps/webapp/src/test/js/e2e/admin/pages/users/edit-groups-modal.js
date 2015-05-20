'use strict';

var Page = require('./edit-groups');

module.exports = Page.extend({

  pageHeader: function() {
    return element(by.css('.modal-header')).getText();
  },

  groupList: function() {
    return element.all(by.repeater('group in availableGroups'));
  },

  selectGroup: function(idx) {
    this.groupList().get(idx).element(by.model('group.checked')).click(); //css('.row-select')).click();
  },

  groupId: function(idx) {
    return this.groupList().get(idx).element(by.css('.group-id')); //binding('{{group.id}}'));
  },

  groupName: function(idx) {
    return this.groupList().get(idx).element(by.css('.group-name'));
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

  addGroup: function(idx) {
    var that = this;
    browser.sleep(500);
    this.selectGroup(idx);
    this.addSelectedGroupButton().click().then(function() {
      that.okButton().click();
    });
  }

});
