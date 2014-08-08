'use strict';

var Page = require('../../page');

module.exports = Page.extend({

  url: '/camunda/app/:webapp/default/#/login',

  formElement: function() {
    return element(by.css('form[name="signinForm"]'));
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
    this.usernameInput().clear();
    this.passwordInput().clear();
    this.usernameInput().sendKeys(username);
    this.passwordInput().sendKeys(password);
    this.loginButton().click();
  }

});