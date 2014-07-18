'use strict';

var Page = require('./editUser');

var formElement = element(by.css('form[name="editProfileForm"]'));

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/users/:user?tab=profile',

  subHeader: function() {
    return formElement.element(by.css('.legend')).getText();
  },

	firstName: function() {
  	return formElement.element(by.model('profile.firstName'));
  },

  lastName: function() { 
  	return formElement.element(by.model('profile.lastName'));
  },

  email: function() {
    return formElement.element(by.model('profile.email'));
  },

  updateProfileButton: function() { 
  	return formElement.element(by.css("button[type='submit']"));
  },

  changeUserProfile: function(firstName, lastName) {
    this.firstName().clear();
    this.lastName().clear();
    this.firstName().sendKeys(firstName);
    this.lastName().sendKeys(lastName);
    this.updateProfileButton().click();
  }

});
