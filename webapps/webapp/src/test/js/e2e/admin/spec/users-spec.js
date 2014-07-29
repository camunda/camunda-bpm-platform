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
      usersPage.login('jonny1', 'jonny1');

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
      usersPage.newUser.isActive();
      expect(usersPage.newUser.pageHeader()).toBe('Create New User');
      expect(usersPage.newUser.createNewUserButton().isEnabled()).toBe(false);
    });


    it('should enter new user data', function() {

      // when
      usersPage.newUser.createNewUser('Icke', 'password1234', 'password1234', 'Ädmün', 'Öttö', 'ädmün.öttö@wurstfarbik.de' );
      usersPage.editUserProfile.navigateTo({ user: 'Icke' });

      // then
      expect(usersPage.editUserProfile.pageHeader()).toBe('Ädmün Öttö');
    });


    it('should login with new account',function() {

      // when
      usersPage.editUserProfile.logout();
      usersPage.login('Icke', 'password1234');

      // then
      expect(usersPage.loggedInUser()).toBe('Icke');
      expect(usersPage.userList().count()).toEqual(1);
    });


    it('should log out', function() {

      usersPage.logout();
    });

  });


  describe('update user Profile', function() {

    it('should validate profile page', function() {

      // given
      usersPage.login('jonny1', 'jonny1');

      // when
      usersPage.selectUser(0);
      usersPage.editUserProfile.selectUserNavbarItem('Profile');

      // then
      expect(usersPage.editUserProfile.subHeader()).toBe('Profile');
      expect(usersPage.editUserProfile.updateProfileButton().isEnabled()).toBe(false);
    });


    it('should update profile', function() {

      // when
      usersPage.editUserProfile.firstName().sendKeys('i');
      usersPage.editUserProfile.updateProfileButton().click();

      // then
      expect(usersPage.editUserProfile.pageHeader()).toBe('Ädmüni Öttö');
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
      usersPage.editUserAccount.myPassword('password1234');
      usersPage.editUserAccount.newPassword('asdfasdf');
      usersPage.editUserAccount.newPasswordRepeat('asdfasdg')

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
      usersPage.login('Icke', 'password1234');

      // then
      expect(usersPage.loggedInUser()).toBe('Icke');
    });


    it('should navigate to Groups menu', function() {

      // when
      usersPage.selectUser(0);
      usersPage.editUserGroups.selectUserNavbarItem('Groups');

      // then
      expect(usersPage.editUserGroups.subHeader()).toBe("Ädmüni Öttö's Groups");
      expect(usersPage.editUserGroups.addGroupButton().isPresent()).toBeFalsy();
    });


    it('should log out', function() {

      usersPage.editUserGroups.logout();
    });

  });


  describe('delete user', function() {

    it('should navigate to Account menu', function() {

      // when
      usersPage.login('jonny1', 'jonny1');
      usersPage.selectUser(0);
      usersPage.editUserAccount.selectUserNavbarItem('Account');

      // then
      expect(usersPage.editUserAccount.subHeaderDeleteUser()).toBe('Delete User');
    });

    it('should delete user', function() {

      // when
      usersPage.editUserAccount.deleteUser();

      // then
      expect(usersPage.userList().count()).toEqual(5);
    });

  });


  describe('end test', function() {

    it('should logout', function() {

      usersPage.logout();
    });

  });

});
