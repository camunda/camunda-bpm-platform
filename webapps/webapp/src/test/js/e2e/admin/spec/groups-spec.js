/*
 * Admin login
 * validate groups main page
 * logout
 * */
'use strict';

var groupsPage = require('../pages/groups');

describe('groups page -', function() {

  describe('start test', function() {

    it('should login', function() {

      // when
      groupsPage.navigateToWebapp('Admin');
      groupsPage.login('demo', 'demo');
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

      // then

      // when
      expect(groupsPage.newGroup.createNewGroupButton().isEnabled()).toBe(false);

      // when
      groupsPage.newGroup.newGroupId().sendKeys('Hallo');
      groupsPage.newGroup.newGroupName().sendKeys('Gruppe');
      // then
      expect(groupsPage.newGroup.createNewGroupButton().isEnabled()).toBe(true);

      // when
      groupsPage.newGroup.newGroupId().clear();
      // then
      expect(groupsPage.newGroup.createNewGroupButton().isEnabled()).toBe(false);

      // when
      groupsPage.newGroup.newGroupName().clear();
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
      groupsPage.editGroup.groupName().sendKeys('i');
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


  describe('end test', function() {

    it('should log out', function () {

      groupsPage.logout();
    });

  });

});
