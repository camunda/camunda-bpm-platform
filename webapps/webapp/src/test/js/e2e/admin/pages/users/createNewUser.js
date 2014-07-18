'use strict';

var Base = require('./newUser');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/user-create',

  createNewUserButton: function() {
    return element(by.css('.btn.btn-primary'));
  },

  createNewUser: function(userId, userPassword, userPasswordRepeat, userFirstName, userLastName, userEmail) {
    this.userId().sendKeys(userId);
    this.password().sendKeys(userPassword);
    this.passwordRepeat().sendKeys(userPasswordRepeat);
    this.userFirstName().sendKeys(userFirstName);
    this.userLastName().sendKeys(userLastName);
    this.userEmail().sendKeys(userEmail);

    this.createNewUserButton().click();
  }

});