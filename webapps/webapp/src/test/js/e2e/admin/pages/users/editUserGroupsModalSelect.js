'use strict';

var Page = require('./editUserGroups');

module.exports = Page.extend({

  pageHeader: function() {
    return element(by.css('.modal-header')).getText();
  },

  groupList: function() {
    return element.all(by.repeater('group in availableGroups'));
  },

  addGoupButton: function() {
    return element(by.css('[ng-click="createGroupMemberships()"]'));
  },

  cancelButton: function() {
    return element(by.css('[ng-click="close()"]'));
  },

  okButton: function() {
    return element(by.css('[ng-click="close(status)"]'));
  },

  addGroup: function(item) {
    this.groupList().get(item).findElement(by.model('group.checked')).click();
    this.addGoupButton().click();

    this.okButton().click();
  }

});
