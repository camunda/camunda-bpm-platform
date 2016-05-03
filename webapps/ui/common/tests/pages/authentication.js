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
  },

  ensureUserLogout: function () {
    var self = this;
    var el = element(by.css('.account.dropdown'));
    el.isPresent().then(function (yepNope) {
      if (yepNope) {
        self.userLogout();
      }
    });
  },

  userLogout: function () {
    element(by.css('.account.dropdown > .dropdown-toggle')).click();
    element(by.css('.account.dropdown > .dropdown-menu > .logout > a')).click();
  }

});
