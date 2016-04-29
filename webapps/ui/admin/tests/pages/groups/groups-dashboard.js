'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/groups',

  newGroupButton: function() {
    return element(by.css('[ng-show="availableOperations.create"] a'));
  },

  groupList: function() {
    return element.all(by.repeater('group in groupList'));
  },

  groupId: function(idx) {
    return this.groupList().get(idx).element(by.css('.group-id > a'));
  },

  groupName: function(idx) {
    return this.groupList().get(idx).element(by.binding('{{group.name}}'));
  },

  groupType: function(idx) {
    return this.groupList().get(idx).element(by.binding('{{group.type}}'));
  },

  selectGroupByEditLink: function(idx) {
    return this.groupList().get(idx).element(by.linkText('Edit')).click();
  },

  selectGroupByNameLink: function(idx) {
    return this.groupId(idx).click();
  }

});
