/*
 * Admin login
 * validate groups main page
 * logout
 * */
'use strict';

var groupsPage = require('../pages/groups');
var usersPage = require('../pages/users');

describe('groups page -', function() {

  describe('start test', function() {

    it('should login', function() {

      console.log('\n' + 'groups-spec');

      // when
      groupsPage.navigateToWebapp('Admin');
      groupsPage.authentication.userLogin('jonny1', 'jonny1');
      groupsPage.selectNavbarItem('Groups');

      // then
      groupsPage.isActive();
      groupsPage.loggedInUser('jonny1');
    });

  });


  describe('groups main page', function() {

    beforeEach(function() {

      groupsPage.navigateTo();
    });


    it('should validate menu', function() {

      // when

      // then
      expect(groupsPage.pageHeader()).toBe('Groups');
      expect(groupsPage.newGroupButton().isPresent()).toBe(true);
      expect(groupsPage.groupList().count()).toBe(4);
    });


    it('should navigate to new group menu', function() {

      // when
      groupsPage.newGroupButton().click();
      groupsPage.newGroup.isActive();

      // then
      expect(groupsPage.newGroup.pageHeader()).toBe('Create New Group');
    });

  });


  describe('create new group menu', function() {

    beforeEach(function() {

      groupsPage.newGroup.navigateTo();
    });


    it('should validate new group menu', function() {

      // when
      expect(groupsPage.newGroup.createNewGroupButton().isEnabled()).toBe(false);

      // when
      groupsPage.newGroup.newGroupIdInput('Hallo');
      groupsPage.newGroup.newGroupNameInput('Gruppe');
      // then
      expect(groupsPage.newGroup.createNewGroupButton().isEnabled()).toBe(true);

      // when
      groupsPage.newGroup.newGroupIdInput().clear();
      // then
      expect(groupsPage.newGroup.createNewGroupButton().isEnabled()).toBe(false);

      // when
      groupsPage.newGroup.newGroupNameInput().clear();
      // then
      expect(groupsPage.newGroup.createNewGroupButton().isEnabled()).toBe(false);
    });


    it('should create new group', function() {

      // when
      groupsPage.newGroup.createNewGroup('4711', 'blaw', 'Marketing');

      // then
      expect(groupsPage.groupList().count()).toBe(5);
    });

  });


  describe('edit group menu', function() {

    it('should navigate to edit group menu', function() {

      // given
      groupsPage.navigateTo();

      // when
      groupsPage.editGroupButton(1).click();

      // then
      expect(groupsPage.editGroup.pageHeader()).toBe('Accounting');
      groupsPage.editGroup.isActive({ group: 'accounting' });
    });


    it('should validate edit group page', function() {

      // when
      groupsPage.editGroup.navigateTo({ group: '4711' });

      // then
      groupsPage.editGroup.isActive({ group: '4711' });
      expect(groupsPage.editGroup.updateGroupButton().isEnabled()).toBe(false);
    });


    it('should edit group', function() {

      // when
      groupsPage.editGroup.groupNameInput('i');
      groupsPage.editGroup.updateGroupButton().click();

      // then
      expect(groupsPage.editGroup.pageHeader()).toBe('blawi');
    });


    it('should delete group', function() {

      // when
      groupsPage.editGroup.deleteGroupButton().click();
      groupsPage.editGroup.deleteGroupAlert().accept();

      // then
      expect(groupsPage.groupList().count()).toBe(4);
    });

  });


  describe('special group names -', function () {

    describe('create groups', function () {

      beforeEach(function () {

        groupsPage.newGroup.navigateTo();
      });


      it('should create group with slash', function () {

        // when
        groupsPage.newGroup.createNewGroup('/göäüp_name', '/üöäüöäü/', 'testgroup/üäö');

        // then
        expect(groupsPage.groupList().count()).toBe(5);
      });


      it('should create group with backslash', function () {

        // when
        groupsPage.newGroup.createNewGroup('\\göäüp_name', '\\üöäüöäü\\', 'testgroup\\üäö');

        // then
        expect(groupsPage.groupList().count()).toBe(6);
      });

    });


    describe('assign groups', function () {

      beforeEach(function () {

        usersPage.navigateTo();
        usersPage.selectUserByNameLink(0);
        usersPage.editUserProfile.selectUserNavbarItem('Groups');
      });


      it('should add slash group to user', function () {

        // when
        usersPage.editUserGroups.addGroupButton().click();
        usersPage.editUserGroups.selectGroup.addGroup(0);

        // then
        expect(groupsPage.groupList().count()).toBe(4);
        expect(usersPage.editUserGroups.groupName(0)).toBe('/göäüp_name');
      });


      it('should add backslash group to user', function () {

        // when
        usersPage.editUserGroups.addGroupButton().click();
        usersPage.editUserGroups.selectGroup.addGroup(0);

        // then
        expect(groupsPage.groupList().count()).toBe(5);
        expect(usersPage.editUserGroups.groupName(1)).toBe('\\göäüp_name');
      });

    });


    describe('remove groups', function () {

      it('should remove group from user', function () {

        // when
        usersPage.editUserGroups.removeGroup(0);

        // then
        expect(usersPage.editUserGroups.groupName(0)).not.toBe('/göäüp_name');
      });


      it('should delete slash group', function () {

        // when
        usersPage.selectNavbarItem('Groups');
        groupsPage.editGroupButton(0).click();
        groupsPage.editGroup.deleteGroup();

        // then
        expect(groupsPage.groupList().count()).toBe(5);
      });


      it('should delete group', function () {

        // when
        groupsPage.editGroupButton(0).click();
        groupsPage.editGroup.deleteGroup();

        // then
        expect(groupsPage.groupList().count()).toBe(4);
      });


      it('should validate users group setting', function () {

        // given
        usersPage.navigateTo();
        usersPage.selectUserByNameLink(0);

        // when
        usersPage.editUserProfile.selectUserNavbarItem('Groups');

        // then
        expect(usersPage.editUserGroups.groupName(0)).not.toBe('\\göäüp_name');
      });

    });

  });


  describe('end test', function() {

    it('should log out', function () {

      groupsPage.logout();
    });

  });

});
