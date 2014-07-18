/*
* Admin login
* validate users main page
* logout 
* */
'use strict';

var usersPage = require('../pages/users');

describe('users page - ', function() {

  describe('start test', function() {

    it('should login', function() {

      // when
      usersPage.navigateToWebapp('Admin');
      usersPage.login('demo', 'demo');

      // then
      usersPage.isActive();
    });

  });


  describe('users main page', function() {

    beforeEach(function() {

      usersPage.navigateTo();
    });


    it('should validate menu', function() {

      // when
      usersPage.selectNavbarItem('Users');

      // then
      usersPage.isActive();
      expect(usersPage.pageHeader()).toBe('Users');
      expect(usersPage.newUserButton().isEnabled()).toBe(true);
    });



  describe('end test', function() {

    it('should log out', function() {

      usersPage.logoutWebapp();
    });

  });

});
