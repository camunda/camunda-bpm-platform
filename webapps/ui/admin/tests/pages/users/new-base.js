'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  userIdInput: function(inputValue) {
    var inputField = element(by.model('profile.id'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  passwordInput: function(inputValue) {
    var inputField = element(by.model('credentials.password'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  passwordRepeatInput: function(inputValue) {
    var inputField = element(by.model('credentials.password2'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  userFirstNameInput: function(inputValue) {
    var inputField = element(by.model('profile.firstName'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  userLastNameInput: function(inputValue) {
    var inputField = element(by.model('profile.lastName'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  userEmailInput: function(inputValue) {
    var inputField = element(by.model('profile.email'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  }

});