'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/groups',

  newGroupButton: function() {
    return element(by.css('.btn.pull-right'));
  },

  groupList: function() {
    return element.all(by.repeater('group in groupList'));
  },

  groupId: function(item) {
    return this.groupList().get(item).element(by.css('.group-id > a'));
  },

  groupName: function(item) {
    return this.groupList().get(item).element(by.binding('{{group.name}}'));
  },

  groupType: function(item) {
    return this.groupList().get(item).element(by.binding('{{group.type}}'));
  },

  selectGroupByEditLink: function(item) {
    return this.groupList().get(item).element(by.linkText('Edit')).click();
  },

  selectGroupByNameLink: function(item) {
    return this.groupId(item).click();
  }

});
