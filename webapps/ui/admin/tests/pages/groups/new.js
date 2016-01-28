'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/group-create',

  newGroupIdInput: function(inputValue) {
    var inputField = element(by.model('group.id'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  newGroupNameInput: function (inputValue) {
    var inputField = element(by.model('group.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  newGroupTypeInput: function (inputValue) {
    var inputField = element(by.model('group.type'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  createNewGroupButton: function () {
    return element(by.css('[ng-click="createGroup()"]'));
  },

  createNewGroup: function (groupID, groupName, groupType) {
    this.newGroupIdInput(groupID);
    this.newGroupNameInput(groupName);
    this.newGroupTypeInput(groupType);
    this.createNewGroupButton().click();
  }
});
