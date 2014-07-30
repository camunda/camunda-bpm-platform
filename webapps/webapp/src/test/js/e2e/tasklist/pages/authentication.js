'use strict';

var Base = require('./base');

module.exports = Base.extend({

  url: '/camunda/app/tasklist/default/#/login',

  formElement: function() {
    return element(by.css('form[name="userLogin"]'));
  },

  loginButton: function() {
    return this.formElement().element(by.css('[type="submit"]'));
  },

  usernameInput: function() {
    return this.formElement().element(by.model('username'));
  },

  passwordInput: function() {
    return this.formElement().element(by.model('password'));
  },

  userLogin: function(username, password) {
    this.usernameInput().sendKeys(username);
    this.passwordInput().sendKeys(password);
    this.loginButton().click();
  }

});