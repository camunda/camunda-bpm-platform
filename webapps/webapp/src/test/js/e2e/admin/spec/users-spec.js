'use strict';

var testSetup = require('../../spec-setup');
var setupFile = require('./users-setup');
testSetup(setupFile);

var users = setupFile.user.create;

var usersPage = require('../pages/users');

describe('Admin - user menu -', function() {

  describe('start test', function() {

    it('should login', function() {

      console.log('\n' + 'users-spec');

      // when
      usersPage.navigateToWebapp('Admin');
      usersPage.authentication.userLogin('admin', 'admin');

      // then
      usersPage.isActive();
    });

  });


  describe('validate users page', function() {

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
      usersPage.selectUserByNameLink(2);

      // then
      usersPage.editUserProfile.isActive({ user: users[1].id });
      expect(usersPage.editUserProfile.pageHeader()).toBe(users[1].firstName + ' ' + users[1].lastName);
      expect(usersPage.editUserProfile.emailInput().getAttribute('value')).toBe(users[1].email);
    });


    it('should select Edit link in list', function() {

      // when
      usersPage.selectUserByEditLink(1);

      // then
      usersPage.editUserProfile.isActive({ user: users[0].id });
      expect(usersPage.editUserProfile.pageHeader()).toBe(users[0].firstName + ' ' + users[0].lastName);
      expect(usersPage.editUserProfile.emailInput().getAttribute('value')).toBe(users[0].email);
    });    

  });


  describe('create new user', function() {

    it('should open Create New User page', function() {

      // given
      usersPage.navigateTo();

      // when
      usersPage.newUserButton().click();

      // then
      usersPage.newUser.isActive();
      expect(usersPage.newUser.pageHeader()).toBe('Create New User');
      expect(usersPage.newUser.createNewUserButton().isEnabled()).toBe(false);
    });


    it('should enter new user data', function() {

      // when
      usersPage.newUser.createNewUser('Icke', 'password1234', 'password1234', 'Cäm', 'Özdemir', 'cäm.özdemir@gruene.de' );
      usersPage.editUserProfile.navigateTo({ user: 'Icke' });

      // then
      expect(usersPage.editUserProfile.pageHeader()).toBe('Cäm Özdemir');
    });


    it('should login with new account',function() {

      // when
      usersPage.editUserProfile.logout();
      usersPage.authentication.userLogin('Icke', 'password1234');

      // then
      expect(usersPage.loggedInUser()).toBe('Icke');
      expect(usersPage.userList().count()).toEqual(1);  //???
    });


    it('should log out', function() {

      usersPage.logout();
    });

  });


  describe('update user Profile', function() {

    it('should validate profile page', function() {

      // given
      usersPage.authentication.userLogin('admin', 'admin');

      // when
      usersPage.selectUser(0);
      usersPage.editUserProfile.selectUserNavbarItem('Profile');

      // then
      expect(usersPage.editUserProfile.subHeader()).toBe('Profile');
      expect(usersPage.editUserProfile.updateProfileButton().isEnabled()).toBe(false);
    });


    it('should update profile', function() {

      // when
      usersPage.editUserProfile.firstNameInput('i');
      usersPage.editUserProfile.updateProfileButton().click();

      // then
      expect(usersPage.editUserProfile.pageHeader()).toBe('Cämi Özdemir');
    });

  });


  describe('update user Account', function() {

    beforeEach(function() {

      usersPage.editUserAccount.navigateTo({ user: 'Icke' });
    });


    it('should validate account page', function() {

      // when
      usersPage.editUserAccount.selectUserNavbarItem('Account');

      // then
      expect(usersPage.editUserAccount.subHeaderChangePassword()).toBe('Change Password');
      expect(usersPage.editUserAccount.changePasswordButton().isEnabled()).toBe(false);
      expect(usersPage.editUserAccount.deleteUserButton().isEnabled()).toBe(true);
    });


    it('should change password (myPassword wrong)', function() {

      // when
      usersPage.editUserAccount.changePassword('blaw', '12345678', '12345678');

      // then
      expect(usersPage.editUserAccount.notification()).toContain('password is not valid.');
    });


    it('should change password (password repeat wrong)', function() {

      // when
      usersPage.editUserAccount.myPasswordInput('password1234');
      usersPage.editUserAccount.newPasswordInput('asdfasdf');
      usersPage.editUserAccount.newPasswordRepeatInput('asdfasdg');

      // then
      expect(usersPage.editUserAccount.changePasswordButton().isEnabled()).toBe(false);
    });


    it('should log out', function() {

      usersPage.editUserAccount.logout();
    });

  });


  describe('groups menu', function() {

    it('should login as non-Admin user', function() {

      // when
      usersPage.authentication.userLogin('Icke', 'password1234');

      // then
      expect(usersPage.loggedInUser()).toBe('Icke');
    });


    it('should navigate to Groups menu', function() {

      // when
      usersPage.selectUser(0);
      usersPage.editUserGroups.selectUserNavbarItem('Groups');

      // then
      expect(usersPage.editUserGroups.subHeader()).toBe("Cämi Özdemir's Groups");
      expect(usersPage.editUserGroups.addGroupButton().isPresent()).toBeFalsy();
    });


    it('should log out', function() {

      usersPage.editUserGroups.logout();
    });

  });


  describe('delete user', function() {

    it('should navigate to Account menu', function() {

      // when
      usersPage.authentication.userLogin('admin', 'admin');
      usersPage.selectUser(0);
      usersPage.editUserAccount.selectUserNavbarItem('Account');

      // then
      expect(usersPage.editUserAccount.subHeaderDeleteUser()).toBe('Delete User');
    });

    it('should delete user', function() {

      // when
      usersPage.editUserAccount.deleteUser();

      // then
      expect(usersPage.userList().count()).toEqual(4);
    });

  });

});
