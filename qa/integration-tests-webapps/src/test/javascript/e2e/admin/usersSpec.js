describe('Admin user settings', function() {
  'use strict';

  var utils = require('./adminUtils');

  function changeUserProfile(firstName, lastName, email) {
    element(by.model('profile.firstName')).clear();
    element(by.model('profile.lastName')).clear();
    element(by.model('profile.email')).clear();
    element(by.model('profile.firstName')).sendKeys(firstName);
    element(by.model('profile.lastName')).sendKeys(lastName);
    element(by.model('profile.email')).sendKeys(email);

    var submitButton = element(by.css("button[type='submit']"));
    submitButton.click();
  }

  function changePassword(oldPassword, newPassword1, newPassword2) {
    element(by.model('credentials.authenticatedUserPassword')).clear();
    element(by.model('credentials.password')).clear();
    element(by.model('credentials.password2')).clear();
    element(by.model('credentials.authenticatedUserPassword')).sendKeys(oldPassword);
    element(by.model('credentials.password')).sendKeys(newPassword1);
    element(by.model('credentials.password2')).sendKeys(newPassword2);
  }

  describe('start test', function () {
    it('should start Admin and login', function () {
      utils.startWebapp('Admin');
      utils.login('jonny1', 'jonny1', true);
    });
  });

 describe('change user profile', function() {
    it('should change user profile', function() {
      utils.selectUserProfile(0, 'Demo', 'Demo');

      changeUserProfile('Ösümä/','ßßß/-üüüü','Ößüä.blaw-blaw@hötmail.nät')

      utils.selectAdminNavbarItem('Users');

      var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));
      expect(items.getText()).toEqual('Ösümä/ ßßß/-üüüü');
    });

    it('should change user name back to Demo Demo', function() {
      utils.selectUserProfile(0, 'Ösümä/', 'ßßß/-üüüü');

      changeUserProfile('Demo','Demo','demo.demo@camunda.com')

      utils.selectAdminNavbarItem('Users');

      var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));
      expect(items.getText()).toEqual('Demo Demo');
    });
  });

  describe('change user password', function() {
    var formElement = element(by.css('form[name="updateCredentialsForm"]'));
    var changePasswordButton = formElement.element(by.css('[type="submit"]'));

    it('should change password with wrong password', function() {
      utils.selectUserProfile(0, 'Demo', 'Demo');
      utils.selectUserNavbarItem('Account');

      changePassword('demo','cam123','cam123');

      expect(changePasswordButton.isEnabled()).toBe(true);
      changePasswordButton.click()

      var notification = element(by.binding('notification.message'));
      expect(notification.getText()).toEqual('Your password is not valid.');
    });

    it('should change password with wrong repetition', function() {
      changePassword('jonny1','cam213','cam123');

      expect(changePasswordButton.isEnabled()).toBe(false);
    });

    it('should change password with correct repetition', function() {
      changePassword('jonny1','cam123','cam123');

      expect(changePasswordButton.isEnabled()).toBe(true);
      changePasswordButton.click()
    });

    it('should relogin with new password', function() {
      utils.logoutWebapp();

      //this is dirty --> no error message appears at first wrong login. Initial click on login button needed
      var submitButton = element(by.css('.btn-primary.btn-large'));
      submitButton.click();

      utils.login('demo','demo', false);
      utils.login('demo','123', false);
      utils.login('demo','cam123', true);
    });

    it('should change password back to origin one with wrong password', function() {
      utils.selectUserProfile(0, 'Demo', 'Demo');
      utils.selectUserNavbarItem('Account');
      changePassword('demo','demo','demo');

      expect(changePasswordButton.isEnabled()).toBe(true);
      changePasswordButton.click()

      var notification = element(by.binding('notification.message'));
      expect(notification.getText()).toEqual('Old password is not valid.');
    });

    it('should change password back to origin one', function() {
      changePassword('cam123','demo','demo');

      expect(changePasswordButton.isEnabled()).toBe(true);
      changePasswordButton.click()
    });

    it('should logout and relogin', function () {
      utils.logoutWebapp();

      //this is dirty --> no error message appears at first wrong login. Initial click on login button needed
      var submitButton = element(by.css('.btn-primary.btn-large'));
      submitButton.click();

      utils.login('demo','cam123', false);
      utils.login('demo','demo', true);
    });
  });

  describe('end test', function () {
    it('should log out', function () {
      utils.logoutWebapp();
    })
  });
});