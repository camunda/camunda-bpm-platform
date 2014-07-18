'use strict';

var Page = require('./../base');

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/groups/:group?tab=group',

  updateGroupButton: function() {
    return element(by.css("button[type='submit']"));
  },

  groupName: function() {
    return element(by.model('group.name'));
  },

  groupType: function() {
    return element(by.model('group.type'));
  },

  deleteGroupButton: function() {
    return element(by.css('.btn-danger'));
  },

  deleteGroupAlert: function() {
    var ptor = protractor.getInstance();
    return ptor.switchTo().alert();
  }

});
