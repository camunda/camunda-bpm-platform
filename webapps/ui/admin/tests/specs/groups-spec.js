'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./groups-setup');

testHelper.expectStringEqual = require('../../../common/tests/string-equal');

var groupsPage = require('../pages/groups');
var usersPage = require('../pages/users');

describe('Admin Groups Spec', function() {


  describe('create new group', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should navigate to new group menu', function() {

      // given
      groupsPage.navigateTo();

      // when
      groupsPage.newGroupButton().click();
      groupsPage.newGroup.isActive();

      // then
      testHelper.expectStringEqual(groupsPage.newGroup.pageHeader(), 'Create New Group');
    });


    it('should create new group', function() {

      // when
      groupsPage.newGroup.createNewGroup('4711', 'blaw', 'Marketing');

      // then
      expect(groupsPage.groupList().count()).to.eventually.eql(5);
      expect(groupsPage.groupId(0).getText()).to.eventually.eql('4711');
      expect(groupsPage.groupName(0).getText()).to.eventually.eql('blaw');
      expect(groupsPage.groupType(0).getText()).to.eventually.eql('Marketing');
    });


    it('should create new group with slash', function() {

      // given
        groupsPage.newGroup.navigateTo();

      // when
        groupsPage.newGroup.createNewGroup('/göäüp_name', '/üöäüöäü/', 'testgroup/üäö');

      // then
        expect(groupsPage.groupList().count()).to.eventually.eql(6);
        expect(groupsPage.groupId(0).getText()).to.eventually.eql('/göäüp_name');
        expect(groupsPage.groupName(0).getText()).to.eventually.eql('/üöäüöäü/');
        expect(groupsPage.groupType(0).getText()).to.eventually.eql('testgroup/üäö');
    });


    it('should select group by name', function() {

      // when
      groupsPage.groupName(0).getText().then(function(groupName) {
        groupsPage.selectGroupByNameLink(0);

        // then
        testHelper.expectStringEqual(groupsPage.editGroup.pageHeader(), groupName);
      });
    });


    it('should create new group with backslash', function() {

      // given
        groupsPage.newGroup.navigateTo();

      // when
        groupsPage.newGroup.createNewGroup('\\göäüp_name', '\\üöäüöäü\\', 'testgroup\\üäö');

      // then
        expect(groupsPage.groupList().count()).to.eventually.eql(7);
        expect(groupsPage.groupId(2).getText()).to.eventually.eql('\\göäüp_name');
        expect(groupsPage.groupName(2).getText()).to.eventually.eql('\\üöäüöäü\\');
        expect(groupsPage.groupType(2).getText()).to.eventually.eql('testgroup\\üäö');
    });


    it('should select group by edit link', function() {

      // when
      groupsPage.groupType(2).getText().then(function(type) {
        groupsPage.selectGroupByEditLink(2);

        // then
        expect(groupsPage.editGroup.groupTypeInput().getAttribute('value')).to.eventually.eql(type);
      });
    });

  });
  
  describe('update/delete group', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
      });
    });


    it('should navigate to edit group menu', function() {

      // given
      groupsPage.navigateTo();

      // when
      groupsPage.selectGroupByNameLink(1);

      // then
      testHelper.expectStringEqual(groupsPage.editGroup.pageHeader(), 'camunda BPM Administrators');
      groupsPage.editGroup.isActive({ group: 'camunda-admin' });
      expect(groupsPage.editGroup.updateGroupButton().isEnabled()).to.eventually.eql(false);
    });


    it('should edit group', function() {

      // when
      groupsPage.editGroup.groupNameInput('i');
      groupsPage.editGroup.updateGroupButton().click();

      // then
      testHelper.expectStringEqual(groupsPage.editGroup.pageHeader(), 'camunda BPM Administratorsi');
    });


    it('should delete group', function() {

      // when
      groupsPage.editGroup.deleteGroup();

      // then
      expect(groupsPage.groupList().count()).to.eventually.eql(3);
    });
  });

  describe('Group Tenants', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should navigate to tenants menu', function() {
      // given
      groupsPage.navigateTo();

      // when
      groupsPage.selectGroupByNameLink(1);
      groupsPage.editGroup.selectUserNavbarItem('Tenants');

      // then
      groupsPage.editGroupTenants.isActive({ group: 'camunda-admin' });
      expect(groupsPage.editGroupTenants.subHeader()).to.eventually
        .eql('camunda BPM Administrators\'s' + ' ' + 'Tenants');
      expect(groupsPage.editGroupTenants.tenantList().count()).to.eventually.eql(0);
    });

    it('should add group to tenant - select tenant modal', function() {

      // when
      groupsPage.editGroupTenants.openAddTenantModal();

      //then
      expect(groupsPage.editGroupTenants.selectTenantModal.pageHeader()).to.eventually.eql('Select Tenants');
      expect(groupsPage.editGroupTenants.selectTenantModal.tenantList().count()).to.eventually.eql(2);
    });
    
    it('should add group to tenants', function() {
      // when
      groupsPage.editGroupTenants.selectTenantModal.addTenant(0);

      // then
      expect(groupsPage.editGroupTenants.tenantId(0)).to.eventually.eql('tenantOne');
      expect(groupsPage.editGroupTenants.tenantList().count()).to.eventually.eql(1);
    });

    it('should show tenants', function() {
      // given
      groupsPage.navigateTo();

      // when
      groupsPage.selectGroupByNameLink(1);
      groupsPage.editGroup.selectUserNavbarItem('Tenants');

      // then
      groupsPage.editGroupTenants.isActive({ group: 'camunda-admin' });
      expect(groupsPage.editGroupTenants.tenantList().count()).to.eventually.eql(1);
    });

    it('should remove tenant', function() {
      // when
      groupsPage.editGroupTenants.removeTenant(0);

      expect(groupsPage.editGroupTenants.tenantList().count()).to.eventually.eql(0);
    });
  });

  describe('Pagination', function () {

    describe('list of groups', function() {

      before(function() {
        return testHelper(setupFile.setup2, function() {
          usersPage.navigateToWebapp('Admin');
          usersPage.authentication.userLogin('admin', 'admin');
          groupsPage.navigateTo();
        });
      });

      it('displays a pager', function () {

        // then
        expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
        expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(2);
      });

    });

    describe('list of users in group', function() {

      before(function() {
        return testHelper(setupFile.setup3, function() {
          usersPage.navigateToWebapp('Admin');
          usersPage.authentication.userLogin('admin', 'admin');
          groupsPage.navigateTo();
        });
      });

      it('displays a pager', function () {

        // when
        groupsPage.selectGroupByNameLink(0);
        groupsPage.editGroup.selectUserNavbarItem('Users');

        // then
        expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
        expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(2);
      });

    });

  });

});
