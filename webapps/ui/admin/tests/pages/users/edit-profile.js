'use strict';

var Page = require('./edit-base');

var formElement = element(by.css('form[name="editProfileForm"]'));

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/users/:user?tab=profile',

  subHeader: function() {
    return formElement.element(by.css('.h4')).getText();
  },

	firstNameInput: function(inputValue) {
    var inputField = element(by.model('profile.firstName'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  lastNameInput: function(inputValue) {
    var inputField = element(by.model('profile.lastName'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  emailInput: function(inputValue) {
    var inputField = element(by.model('profile.email'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  updateProfileButton: function() {
  	return formElement.element(by.css('[ng-click="updateProfile()"]'));
  },

  changeUserProfile: function(firstName, lastName) {
    this.firstNameInput().clear();
    this.lastNameInput().clear();
    this.firstNameInput(firstName);
    this.lastNameInput(lastName);
    this.updateProfileButton().click();
  }

});
