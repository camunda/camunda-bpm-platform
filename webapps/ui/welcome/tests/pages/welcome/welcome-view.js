'use strict';

var Base = require('./../base');

module.exports = Base.extend({
  url: '/camunda/app/welcome/default/#/welcome',



  webappLinks: function() {
    return element(by.css('.webapps'));
  },

  adminWebappLink: function() {
    return this.webappLinks().element(by.css('.admin-app'));
  },

  cockpitWebappLink: function() {
    return this.webappLinks().element(by.css('.cockpit-app'));
  },

  tasklistWebappLink: function() {
    return this.webappLinks().element(by.css('.tasklist-app'));
  },





  userProfile: function() {
    return element(by.css('#user-profile'));
  },

  userProfileFullName: function() {
    return this.userProfile().element(by.css('.user-profile-name'));
  },

  userProfileEmail: function() {
    return this.userProfile().element(by.css('.user-profile-email'));
  },

  userProfileGroups: function() {
    return this.userProfile().element(by.css('.user-profile-groups'));
  },

  userProfileLink: function() {
    return this.userProfile().element(by.cssContainingText('.action-links li a', 'Edit profile'));
  },

  changePasswordLink: function() {
    return this.userProfile().element(by.cssContainingText('.action-links li a', 'Change password'));
  },



  userProfileForm: function() {
    return this.userProfile().element(by.css('form[name=userProfile]'));
  },

  userProfileFirstNameField: function() {
    return this.userProfileForm().element(by.css('[name=firstName]'));
  },

  userProfileLastNameField: function() {
    return this.userProfileForm().element(by.css('[name=lastName]'));
  },

  userProfileEmailField: function() {
    return this.userProfileForm().element(by.css('[name=email]'));
  },

  userProfileFormSubmit: function() {
    return this.userProfileForm().element(by.css('[type=submit]')).click();
  },



  changePasswordForm: function() {
    return this.userProfile().element(by.css('form[name=changePassword]'));
  },

  changePasswordCurrentField: function() {
    return this.changePasswordForm().element(by.css('[name=current]'));
  },

  changePasswordNewField: function() {
    return this.changePasswordForm().element(by.css('[name=new]'));
  },

  changePasswordConfirmationField: function() {
    return this.changePasswordForm().element(by.css('[name=confirmation]'));
  },

  changePasswordFormSubmit: function() {
    return this.changePasswordForm().element(by.css('[type=submit]')).click();
  }
});
