'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./tenant-setup');

testHelper.expectStringEqual = require('../../../common/tests/string-equal');

var tenantsPage = require('../pages/tenants');
var usersPage = require('../pages/users');
var groupsPage = require('../pages/groups');

describe('Admin Tenants Spec', function() {
  describe('create new tenant', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        tenantsPage.navigateToWebapp('Admin');
        tenantsPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should navigate to new tenant menu', function() {

      // given
      tenantsPage.navigateTo();

      // when
      tenantsPage.newTenantButton().click();
      tenantsPage.newTenant.isActive();

      // then
      testHelper.expectStringEqual(tenantsPage.newTenant.pageHeader(), 'Create New Tenant');
    });


    it('should create new tenant', function() {

      // given
      tenantsPage.newTenant.navigateTo();

      // when
      tenantsPage.newTenant.createNewTenant('4711', 'blaw');

      // then
      expect(tenantsPage.tenantList().count()).to.eventually.eql(3);
      expect(tenantsPage.tenantId(0).getText()).to.eventually.eql('4711');
      expect(tenantsPage.tenantName(0).getText()).to.eventually.eql('blaw');
    });


    it('should create new tenant with slash', function() {

      // given
      tenantsPage.newTenant.navigateTo();

      // when
      tenantsPage.newTenant.createNewTenant('/töäünöäünt_name', '/töäünöäünt/');

      // then
        expect(tenantsPage.tenantList().count()).to.eventually.eql(4);
        expect(tenantsPage.tenantId(0).getText()).to.eventually.eql('/töäünöäünt_name');
        expect(tenantsPage.tenantName(0).getText()).to.eventually.eql('/töäünöäünt/');
    });


    it('should select tenant by name', function() {

      // when
      tenantsPage.tenantName(0).getText().then(function(tenantName) {
        tenantsPage.selectTenantByNameLink(0);

        // then
        testHelper.expectStringEqual(tenantsPage.editTenant.pageHeader(), tenantName);
      });
    });


    it('should create new tenant with backslash', function() {

      // given
        tenantsPage.newTenant.navigateTo();

      // when
      tenantsPage.newTenant.createNewTenant('\\töäünöäünt_name', '\\töäünöäünt\\');

      // then
        expect(tenantsPage.tenantList().count()).to.eventually.eql(5);
        expect(tenantsPage.tenantId(2).getText()).to.eventually.eql('\\töäünöäünt_name');
        expect(tenantsPage.tenantName(2).getText()).to.eventually.eql('\\töäünöäünt\\');
    });


    it('should select tenant by edit link', function() {

      // when
      tenantsPage.tenantName(2).getText().then(function(tenantName) {
        tenantsPage.selectTenantByEditLink(2);

        // then
        testHelper.expectStringEqual(tenantsPage.editTenant.pageHeader(), tenantName);
      });
    });

  });


  describe('update/delete tenant', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        tenantsPage.navigateToWebapp('Admin');
        tenantsPage.authentication.userLogin('admin', 'admin');
      });
    });


    it('should navigate to edit tenant menu', function() {

      // given
      tenantsPage.navigateTo();

      // when
      tenantsPage.selectTenantByNameLink(1);

      // then
      testHelper.expectStringEqual(tenantsPage.editTenant.pageHeader(), 'Tenant Two');
      tenantsPage.editTenant.isActive({ tenant: 'tenantTwo' });
      expect(tenantsPage.editTenant.updateTenantButton().isEnabled()).to.eventually.eql(false);
    });


    it('should edit tenant', function() {

      // when
      tenantsPage.editTenant.tenantNameInput('i');
      tenantsPage.editTenant.updateTenantButton().click();

      // then
      testHelper.expectStringEqual(tenantsPage.editTenant.pageHeader(), 'Tenant Twoi');
    });


    it('should delete tenant', function() {

      // when
      tenantsPage.editTenant.deleteTenant();

      // then
      expect(tenantsPage.tenantList().count()).to.eventually.eql(1);
    });

  });

  describe('Pagination', function () {

    describe('list of tenants in add tenants to user modal', function() {
      before(function() {
        return testHelper(setupFile.setup5, function() {
          tenantsPage.navigateToWebapp('Admin');
          tenantsPage.authentication.userLogin('admin', 'admin');

          usersPage.navigateTo();
        });
      });
      
      it('displays a pager', function () {
        // given
        usersPage.selectUserByEditLink(0);
        usersPage.editUserProfile.selectUserNavbarItem('Tenants');

        // when
        usersPage.editUserTenants.openAddTenantModal();

        // then
        expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
        expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(3);
      });
    });

    describe('list of tenants in add tenants to group modal', function() {
      before(function() {
        return testHelper(setupFile.setup5, function() {
          tenantsPage.navigateToWebapp('Admin');
          tenantsPage.authentication.userLogin('admin', 'admin');

          groupsPage.navigateTo();
        });
      });

      it('displays a pager', function () {

        // given
        groupsPage.selectGroupByEditLink(0);
        groupsPage.editGroup.selectUserNavbarItem('Tenants');

        // when
        groupsPage.editGroupTenants.openAddTenantModal();

        // then
        expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
        expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(3);
      });
    });

    describe('list of tenants', function() {

      before(function() {
        return testHelper(setupFile.setup2, function() {
          tenantsPage.navigateToWebapp('Admin');
          tenantsPage.authentication.userLogin('admin', 'admin');
          tenantsPage.navigateTo();
        });
      });

      it('displays a pager', function () {

        // then
        expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
        expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(2);
      });

    });

    describe('list of users in tenant', function() {

      before(function() {
        return testHelper(setupFile.setup4, function() {
          tenantsPage.navigateToWebapp('Admin');
          tenantsPage.authentication.userLogin('admin', 'admin');
          tenantsPage.navigateTo();
        });
      });

      it('displays a pager', function () {

        // when
        tenantsPage.selectTenantByNameLink(0);
        tenantsPage.editTenant.selectUserNavbarItem('Users');

        // then
        expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
        expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(2);
      });

    });

    describe('list of groups in tenant', function() {

      before(function() {
        return testHelper(setupFile.setup3, function() {
          tenantsPage.navigateToWebapp('Admin');
          tenantsPage.authentication.userLogin('admin', 'admin');
          tenantsPage.navigateTo();
        });
      });

      it('displays a pager', function () {

        // when
        tenantsPage.selectTenantByNameLink(0);
        tenantsPage.editTenant.selectUserNavbarItem('Groups');

        // then
        expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
        expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(2);
      });

    });

  });

});
