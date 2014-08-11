/*
 * Admin login
 * remove admin group from admin user
 * create new admin user
 * add admin froup to origin admin user
 * delete interim admin user
 * */
'use strict';

var usersPage = require('../pages/users');

describe('admin user -', function() {

  describe('start test', function() {

    it('should login', function() {

      console.log('\n' + 'administrator-spec');

      // when
      usersPage.navigateToWebapp('Admin');
      usersPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      usersPage.isActive();
    });

  });


  describe('remove current admin user rights', function() {

    it('should select user', function() {

      // when
      usersPage.selectUserByEditLink(2);

      // then
      expect(usersPage.editUserGroups.pageHeader()).toBe('Jonny Prosciutto');
    });


    it('should remove admin group', function() {

      // given
      usersPage.editUserGroups.selectUserNavbarItem('Groups');

      // when
      usersPage.editUserGroups.removeGroup(0);

      // then
      expect(usersPage.editUserGroups.groupList().count()).toEqual(0);
    });

  });


  describe('validate initial admin setup', function() {

    it('should validate Setup page', function() {

      // given
      usersPage.logout();

      // when
      usersPage.navigateToWebapp('Admin');

      // then
      expect(usersPage.adminUserSetup.pageHeader()).toBe('Setup');
      expect(usersPage.adminUserSetup.createNewAdminButton().isEnabled()).toBe(false);
    });


    it('should enter new admin profile', function() {

      // when
      usersPage.adminUserSetup.userId().sendKeys('Admin');
      usersPage.adminUserSetup.password().sendKeys('admin123');
      usersPage.adminUserSetup.passwordRepeat().sendKeys('admin123');
      usersPage.adminUserSetup.userFirstName().sendKeys('Über');
      usersPage.adminUserSetup.userLastName().sendKeys('Admin');
      usersPage.adminUserSetup.userEmail().sendKeys('uea@camundo.org');

      usersPage.adminUserSetup.createNewAdminButton().click();

      // then
      expect(usersPage.adminUserSetup.statusMessage()).toBe('User created You have created an initial user.');
    });


    it('should login page as Admin', function() {

      // when
      usersPage.navigateToWebapp('Admin');
      usersPage.authentication.userLogin('Admin', 'admin123');

      // then
      expect(usersPage.userFirstNameAndLastName(0)).toBe('Über Admin');
    });

  });


  describe('reassign admin user rights', function() {

    it('should open group select page', function() {

      // given
      usersPage.selectUserByEditLink(3);
      usersPage.editUserGroups.selectUserNavbarItem('Groups');

      // when
      usersPage.editUserGroups.addGroupButton().click();

      // then
      expect(usersPage.editUserGroups.selectGroup.pageHeader()).toBe('Select Groups');
    });


    it('should add camunda-admin group', function() {

      // when
      usersPage.editUserGroups.selectGroup.addGroup(1);

      // then
      expect(usersPage.editUserGroups.groupList().count()).toEqual(1);
    });

  });


  describe('remove interim admin', function() {

    it('should delete user account', function() {

      // given
      usersPage.navigateTo();
      usersPage.selectUserByEditLink(0);
      usersPage.editUserAccount.selectUserNavbarItem('Account');

      // when
      usersPage.editUserAccount.deleteUserButton().click();
      usersPage.editUserAccount.deleteUserAlert().accept();

      // then
      expect(usersPage.userList().count()).toEqual(5);
     });

  });


  describe('end test', function() {

    it('should log out', function() {

      usersPage.logout();
    })

  });

});