describe('Admin initial user setup', function() {
  'use strict';

  var utils = require('./adminUtils');

  describe('start test', function () {
    it('should start Admin and login', function () {
      utils.startWebapp('Admin');
      utils.login('jonny1', 'jonny1', true);
    });
  });

  describe('remove current admin user rights', function () {
    it('should go to user groups settings', function () {
      utils.selectUserProfile(2, 'Jonny', 'Prosciutto');
      utils.selectUserNavbarItem('Groups');
    });

    it('should remove admin group and log out', function () {
      var removeGroupButton = element(by.css('.btn.btn-small'));
      removeGroupButton.click();

      utils.logoutWebapp();
      //...and force page refresh
      utils.startWebapp('Admin');
    });
  });

  describe('validate intial admin setup', function () {
    it('should validate Setup page', function () {
      expect(element(by.css('.page-header')).getText()).toEqual('Setup');
    });

    it('should enter new admin profile', function () {
      element(by.model('profile.id')).sendKeys('Admin');
      element(by.model('credentials.password')).sendKeys('admin123');
      element(by.model('credentials.password2')).sendKeys('admin123');
      element(by.model('profile.firstName')).sendKeys('Stefan');
      element(by.model('profile.lastName')).sendKeys('H.');
      element(by.model('profile.email')).sendKeys('sh@camundo.org');

      element(by.css('.btn.btn-primary')).click();
    });

    it('should go to login page', function () {
      element(by.css('.brand')).click();
    });

    it('should log in as admin', function () {
      utils.login('Admin', 'admin123', true);
    });
  });

  describe('reassign admin user rights', function () {
    it('should go to user groups settings', function () {
      utils.selectUserProfile(3, 'Jonny', 'Prosciutto');
      utils.selectUserNavbarItem('Groups');
    });

    it('should add user to admin group', function () {
      element(by.css('[name="updateGroupMemberships"] .pull-right')).click();
    });

    it('should select camunda-admin group', function () {
      var groups = element.all(by.repeater('group in availableGroups'));

      groups.get(1).findElement(by.model('group.checked')).click();
      element(by.css('[ng-click="createGroupMemberships()"]')).click();

      //TODO validate message
      element(by.css('[ng-click="close(status)"]')).click();
    });
  });

  describe('remove interim admin', function () {
    it('should go to user account settings', function () {
      utils.selectAdminNavbarItem('Users');
      utils.selectUserProfile(0, 'Stefan', 'H.');
      utils.selectUserNavbarItem('Account');
    });

    it('should delete user account', function () {
      var deleteUserButton = element(by.css('.btn-danger'));
      deleteUserButton.click();

      var ptor = protractor.getInstance();
      var alertDialog = ptor.switchTo().alert();

      expect(alertDialog.getText()).toEqual("Really delete user Admin?");

      alertDialog.accept();
    });
  });

  describe('end test', function () {
    it('should log out', function () {
      utils.logoutWebapp();
    })
  });
});
