'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/users',

  newUserButton: function() {
    return element(by.css('[ng-show="availableOperations.create"]'));
  },

  userList: function() {
    return element.all(by.repeater('user in userList'));
  },

  userFirstNameAndLastName: function(idx) {
    return element(by.repeater('user in userList').row(idx).column('{{user.firstName}} {{user.lastName}}')).getText();
  },

	selectUser: function(idx) {
		this.selectUserByEditLink(idx);
	},

  selectUserByEditLink: function(idx) {
    this.userList().get(idx).element(by.linkText('Edit')).click();
  },

  selectUserByNameLink: function(idx) {
    this.userList().get(idx).element(by.binding('{{user.firstName}} {{user.lastName}}')).click();
  }

});
