'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/group-create',

  newGroupId: function() {
    return element(by.model('group.id'));
  },

  newGroupName: function () {
    return element(by.model('group.name'));
  },

  newGroupType: function () {
    return element(by.model('group.type'));
  },

  createNewGroupButton: function () {
    return element(by.css("button[type='submit']"));
  },

  createNewGroup: function (groupID, groupName, groupType) {
    this.newGroupId().sendKeys(groupID);
    this.newGroupName().sendKeys(groupName);
    this.newGroupType().sendKeys(groupType);
    this.createNewGroupButton().click();
  }
});
