'use strict';

var Page = require('./page');

module.exports = Page.extend({

  url: '/camunda/app/:webapp/default/#/login',

  formElement: function() {
    return element(by.css('form[name="signinForm"]'));
  },

  loginButton: function() {
    return this.formElement().element(by.css('[type="submit"]'));
  },

  usernameInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('username'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  passwordInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('password'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  userLogin: function(username, password) {
    this.usernameInput().clear();
    this.passwordInput().clear();
    this.usernameInput(username);
    this.passwordInput(password);
    this.loginButton().click();
  }

});