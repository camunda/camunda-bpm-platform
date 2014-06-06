describe('admin dashboard', function() {
  'use strict';
  var utils = require('./../utils');

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

  function selectUserProfile(rowIndex, firstName, lastName) {
    var items = element(by.repeater('user in userList').row(rowIndex).column('{{user.firstName}} {{user.lastName}}'));

    expect(items.getText()).toEqual(firstName + ' ' + lastName);
    items.click();
  }

  describe('start page', function() {

    browser.get('camunda/app/admin/');
    browser.driver.manage().window().maximize();

    it('should load the home page', function() {
      var appName = element(by.css('.brand'));

      expect(appName.getText()).toEqual('camunda Admin');
    });
  });

  describe('user login', function() {
    it('should validate credentials', function () {
      element(by.model('username')).clear();
      element(by.model('password')).clear();
      element(by.model('username')).sendKeys('jonny1');
      element(by.model('password')).sendKeys('jonny1');

      element(by.css('.btn-primary.btn-large')).click();
    });
  });

  xdescribe('change user profile', function() {
    it('should select the first user', function() {
      selectUserProfile(0, 'Demo', 'Demo');
    });

    it('should change user profile', function() {
      changeUserProfile('Vogel','Strauß','vogel.strauß@hotmail.net')

      // validate change
      element(by.css('.navbar ul li:nth-child(1)')).click();

      var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));
      expect(items.getText()).toEqual('Vogel Strauß');
    });

    it('should change user profile back to Demo Demo', function() {
      selectUserProfile(0, 'Vogel', 'Strauß');
      changeUserProfile('Demo','Demo','demo.demo@camunda.com')

      // validate changes
      element(by.css('.navbar ul li:nth-child(1)')).click();

      var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));
      expect(items.getText()).toEqual('Demo Demo');
    });
  });

  describe('start webapps without admin profile', function() {
    it('should remove admin rights', function() {
      selectUserProfile(2, 'Jonny', 'Prosciutto');

      element(by.css('.sidebar-nav ul li:nth-child(3)')).click();
      element(by.css('.btn.btn-small')).click();
    });

    it('should log out', function() {
      element(by.css('.navbar [sem-show-user-actions]')).click();
      element(by.css('.navbar [sem-log-out]')).click();

      //...and force page refresh
      element(by.css('.navbar [sem-show-apps]')).click();
      element(by.css('.navbar [sem-jump-to-cockpit]')).click();
    });

    it('should validate Setup page', function() {
      expect(element(by.css('.page-header')).getText()).toEqual('Setup');
    });

    it('should enter new admin profile', function() {
      element(by.model('profile.id')).sendKeys('Admin');
      element(by.model('credentials.password')).sendKeys('admin123');
      element(by.model('credentials.password2')).sendKeys('admin123');
      element(by.model('profile.firstName')).sendKeys('Stefan');
      element(by.model('profile.lastName')).sendKeys('H.');
      element(by.model('profile.email')).sendKeys('sh@camundo.org');

      element(by.css('.btn.btn-primary')).click();
    });

    it('should go to login page', function() {
      element(by.css('.brand')).click();
    });

    it('should log in', function() {
      element(by.model('username')).sendKeys('Admin');
      element(by.model('password')).sendKeys('admin123');
      element(by.css('.btn-primary.btn-large')).click();
    });

    it('should add jonny to admin group', function() {
      selectUserProfile(3, 'Jonny', 'Prosciutto');
      element(by.css('.sidebar-nav ul li:nth-child(3)')).click();
      element(by.css('[name="updateGroupMemberships"] .pull-right')).click();
    });

    it('should add camunda-admin group', function() {
      var groups = element.all(by.repeater('group in availableGroups'));

      groups.get(1).findElement(by.model('group.checked')).click();
      element(by.css('[ng-click="createGroupMemberships()"]')).click();

      //TODO validate message
      element(by.css('[ng-click="close(status)"]')).click();
    });

    it('should delete interim admin', function() {
      element(by.css('.navbar ul li:nth-child(1)')).click();
      selectUserProfile(0, 'Stefan', 'H.');
      element(by.css('.sidebar-nav ul li:nth-child(2)')).click();
      element(by.css('.btn-danger')).click();
      var ptor = protractor.getInstance();
      var alertDialog = ptor.switchTo().alert();

      expect(alertDialog.getText()).toEqual("Really delete user Admin?");
      alertDialog.accept();

      element(by.css('.navbar [sem-show-user-actions]')).click();
      element(by.css('.navbar [sem-log-out]')).click();
    });
  });


});