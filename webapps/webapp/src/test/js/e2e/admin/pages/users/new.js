'use strict';

var Base = require('./new-base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/user-create',

  createNewUserButton: function() {
    return element(by.css('[ng-click="createUser()"]'));
  },

  createNewUser: function(userId, userPassword, userPasswordRepeat, userFirstName, userLastName, userEmail) {
    this.userIdInput(userId);
    this.passwordInput(userPassword);
    this.passwordRepeatInput(userPasswordRepeat);
    this.userFirstNameInput(userFirstName);
    this.userLastNameInput(userLastName);
    this.userEmailInput(userEmail);

    this.createNewUserButton().click();
  }

});
