'use strict';

var Page = require('./editUser');

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/users/:user?tab=groups',

  groupList: function() {
    return element.all(by.repeater('group in groupList'));
  },

  addGroupButton: function() {
    return element(by.css('.btn.pull-right'));
  },

  removeGroup: function(item) {
    this.groupList().get(item).findElement(by.css('.btn.btn-small')).click();
  }
});
