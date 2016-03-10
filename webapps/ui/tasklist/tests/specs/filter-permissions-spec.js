'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./filter-permissions-setup');

var dashboardPage = require('../pages/dashboard');
var editModalPage = dashboardPage.taskFilters.editFilterPage;


describe('Tasklist Filter Permissions Spec', function() {

  describe('the permissions page', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    beforeEach(function() {
      dashboardPage.taskFilters.selectFilter(0);
      dashboardPage.taskFilters.editFilter(0);
      editModalPage.selectPanelByKey('permission');
    });

    afterEach(function() {
      editModalPage.closeFilter();
    });


    it('should contain elements', function() {

      // given
      expect(editModalPage.permissionHelpText()).to.eventually.eql('This section is aimed to set read permissions for the filter.');
      expect(editModalPage.accessibleByAllUsersCheckBox().isSelected()).to.eventually.be.false;
      expect(editModalPage.addPermissionButton().isDisplayed()).to.eventually.be.true;
    });


    it('should make filter accessible for all users', function() {

      // given
      expect(editModalPage.accessibleByAllUsersCheckBox().isSelected()).to.eventually.be.false;

      // when
      editModalPage.accessibleByAllUsersCheckBox().click()

      // then
      expect(editModalPage.accessibleByAllUsersCheckBox().isSelected()).to.eventually.be.true;
    });


    it('should allow to add a permission for users', function() {

      // when
      editModalPage.addPermission('user', 'franz');

      // then
      expect(editModalPage.getPermissionType()).to.eventually.eql('user');
    });


    it('should allow to add a permission for groups', function() {

      // when
      editModalPage.addPermission('group', 'marketing');

      // then
      expect(editModalPage.getPermissionType()).to.eventually.eql('group');
    });


    it('should keep entered data when switching accordion tab', function() {

      // given
      editModalPage.addPermission('user', 'hubert');

      // when
      editModalPage.selectPanelByKey('general');
      editModalPage.selectPanelByKey('permission');

      // then
      expect(editModalPage.getPermissionType(0)).to.eventually.eql('user');
      expect(editModalPage.getPermissionId(0)).to.eventually.eql('hubert');
    });


    it('should validate unique permissions', function() {

      // when
      editModalPage.addPermission('group', 'sales');
      editModalPage.addPermission('group', 'sales');

      // then
      expect(editModalPage.permissionIdHelpText()).to.eventually.eql('Given group has already read permissions');
    });


    it('should allow to remove permissions', function() {

      // given
      editModalPage.addPermission('user', 'franz');
      editModalPage.addPermission('user', 'hubert');
      editModalPage.addPermission('group', 'sales')

      // when
      editModalPage.removePermissionButton(0).click();

      // then
      expect(editModalPage.permissionList().count()).to.eventually.eql(1);
      expect(editModalPage.getPermissionId(0)).to.eventually.eql('hubert');
      expect(editModalPage.getPermissionId()).to.eventually.eql('sales');
    });

  });


  describe('assign permissions', function() {

    describe('create user permission', function() {

      before(function() {
        return testHelper(setupFile.setup1, function() {

          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('admin', 'admin');
        });
      });

      afterEach(function() {
        editModalPage.logout();
      });

      it('should save new permission', function() {

        // given
        dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
        editModalPage.selectPanelByKey('permission');

        // when
        editModalPage.addPermission('user', 'test');

        // then
        editModalPage.saveFilter();
      });


      it('should validate permission', function() {

        // when
        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');

        // then
        expect(dashboardPage.taskFilters.filterName(0)).to.eventually.include('Empty Filter');
      });

    });


    describe('create group permission', function() {

      before(function() {
        return testHelper(setupFile.setup1, function() {

          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('admin', 'admin');
        });
      });

      afterEach(function() {
        editModalPage.logout();
      });

      it('should save new permission', function() {

        // given
        dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
        editModalPage.selectPanelByKey('permission');

        // when
        editModalPage.addPermission('group', 'marketing');

        // then
        editModalPage.saveFilter();
      });


      it('should validate permission', function() {

        // when
        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('juri', 'juri');

        // then
        expect(dashboardPage.taskFilters.filterName(0)).to.eventually.include('Empty Filter');
      });

    });


    describe('make filter accessible for all user', function() {

      before(function() {
        return testHelper(setupFile.setup1, function() {

          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('admin', 'admin');
        });
      });

      afterEach(function() {
        editModalPage.logout();
      });

      it('should select accessible checkbox', function() {

        // given
        dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
        editModalPage.selectPanelByKey('permission');

        // when
        editModalPage.accessibleByAllUsersCheckBox().click();

        // then
        editModalPage.saveFilter();
      });


      it('should validate permission', function() {

        // when
        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');

        // then
        expect(dashboardPage.taskFilters.filterName(0)).to.eventually.include('Empty Filter');
      });

    });

  });

});
