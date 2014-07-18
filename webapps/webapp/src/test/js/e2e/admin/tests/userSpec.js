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


    it('should select user name in list', function() {

      // when
      usersPage.selectUserByNameLink(0);

      // then
      usersPage.editUserProfile.isActive({ user: 'demo' });
    });


    it('should select Edit link in list', function() {

      // when
      usersPage.selectUserByEditLink(1);

      // then
      usersPage.editUserProfile.isActive({ user: 'john' });
    });    

  });


  describe('create new user', function() {

    it('should open Create New User page', function() {

      // given
      usersPage.navigateTo();

      // when
      usersPage.newUserButton().click()

      // then
      usersPage.createNewUser.isActive();
      expect(usersPage.createNewUser.pageHeader()).toBe('Create New User');
      expect(usersPage.createNewUser.createNewUserButton().isEnabled()).toBe(false);
    });


    it('should enter new user profile', function() {

      // when
      usersPage.createNewUser.createNewUser('Icke', 'password1234', 'password1234', 'Ädmün', 'Öttö', 'ädmün.öttö@wurstfarbik.de' );
      usersPage.editUserProfile.navigateTo('Icke');

      // then
      expect(usersPage.editUserProfile.pageHeader()).toBe('Admün Öttö');
    });

    it('should relogin with new account',function() {

      // when
      usersPage.editUserProfile.logout();
      usersPage.login('Icke', 'password1234');

      // then
      expect(usersPage.userList().count()).toEqual(1);
    });

  });


  xdescribe('update user profile', function() {

    it('should validate profile page', function() {

      // when
      usersPage.selectUser(0);
      usersPage.editUserProfile.selectUserNavbarItem('Profile');

      // then
      expact(usersPage.editUserProfile.subHeader()).toBe('Profile');
      expact(usersPage.editUserProfile.updateProfileButton().isEnabled()).toBe(false);
    });


    it('should update profile', function() {

      // when
      usersPage.editUserProfile.firstName().sendKeys('i');
      usersPage.editUserProfile.updateProfileButton().click();

      // then
      expect(usersPage.editUserProfile.pageHeader()).toBe('Admüni Öttö');
    });


    it('should validate account page', function() {

      // when
      usersPage.editUserAccount.selectUserNavbarItem('Account');

      // then
      expect(usersPage.editUserAccount.subHeader()).toBe('Change Password');
      expect(usersPage.editUserAccount.changePasswordButton().isEnabled).toBe(false);
      expect(usersPage.editUserAccount.deleteUserButton().isEnabled).toBe(true);
    });

  });


  describe('end test', function() {

    it('should log out', function() {

      usersPage.logoutWebapp();
    });

  });

});
