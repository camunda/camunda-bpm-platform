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

  editGroupButton: function(item) {
    return this.groupList().get(item).findElement(by.linkText('Edit'));
  }

});
