'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./authorizations-setup');

var authorizationsPage = require('../pages/authorizations');
var cockpitPage = require('../../cockpit/pages/dashboard');


describe('Admin Authorizations Spec', function() {

  function checkCreateNewState() {

    // then
    expect(authorizationsPage.createNewElement().isDisplayed()).to.eventually.be.true;
    expect(authorizationsPage.submitNewAuthorizationButton().isEnabled()).to.eventually.be.false;
    expect(authorizationsPage.abortNewAuthorizationButton().isEnabled()).to.eventually.be.true;
    expect(authorizationsPage.resourceIdField().getAttribute('value')).to.eventually.eql('*');
  }

  function checkAuthorizationTypes() {

    authorizationsPage.authorizationType('GLOBAL').click();
    expect(authorizationsPage.identityIdInputFiled().isEnabled()).to.eventually.be.false;

    authorizationsPage.authorizationType('DENY').click();
    expect(authorizationsPage.identityIdInputFiled().isEnabled()).to.eventually.be.true;

    authorizationsPage.authorizationType('ALLOW').click();
    expect(authorizationsPage.identityIdInputFiled().isEnabled()).to.eventually.be.true;
  }

  function checkPermissionTypes(permissionsList, permissionDefaultValue) {

    // when
    authorizationsPage.permissionsButton().click();

    // then
    for (var i = 0; i < permissionsList.length; i++) {
      expect(authorizationsPage.permissionsDropdownList().get(i).getText()).to.eventually.eql(permissionsList[i]);
    }

    if (arguments.length !== 2) {
      permissionDefaultValue = 'ALL';
    }
    expect(authorizationsPage.permissionsField().getText()).to.eventually.eql(permissionDefaultValue);
  }

  function abortCreatingNewAuthorization() {
    authorizationsPage.abortNewAuthorizationButton().click();
  }

  describe('application authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to application page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Application');

      // then
      authorizationsPage.application.isActive();
      expect(authorizationsPage.application.createNewButton().isEnabled()).to.eventually.be.true;
      expect(authorizationsPage.application.newAuthorizationButton().isEnabled()).to.eventually.be.true;
      expect(authorizationsPage.application.boxHeader()).to.eventually.eql('Application Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.newAuthorizationButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'ACCESS',
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });


    it('should create new application authorization', function() {

      // when
      authorizationsPage.application.createNewAuthorization('ALLOW', 'USER', 'john', 'ACCESS', 'cockpit');

      // then
      authorizationsPage.logout();
      cockpitPage.navigateToWebapp('Cockpit');
      cockpitPage.authentication.userLogin('john', 'MobyDick');
      cockpitPage.isActive();
    });

  });


  describe('Authorization Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to authorization page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Authorization');

      // then
      authorizationsPage.authorization.isActive();
      expect(authorizationsPage.authorization.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.authorization.boxHeader()).to.eventually.eql('Authorization Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'READ',
          'UPDATE',
          'CREATE',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('Deployment Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to deployment page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Deployment');

      // then
      authorizationsPage.deployment.isActive();
      expect(authorizationsPage.deployment.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.deployment.boxHeader()).to.eventually.eql('Deployment Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'CREATE',
          'READ',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('Filter Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to filter page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Filter');

      // then
      authorizationsPage.filter.isActive();
      expect(authorizationsPage.filter.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.filter.boxHeader()).to.eventually.eql('Filter Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'READ',
          'UPDATE',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('Group Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to group page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Group');

      // then
      authorizationsPage.group.isActive();
      expect(authorizationsPage.group.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.group.boxHeader()).to.eventually.eql('Group Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'READ',
          'UPDATE',
          'CREATE',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('Group Membership Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to group membership page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Group Membership');

      // then
      authorizationsPage.groupMembership.isActive();
      expect(authorizationsPage.groupMembership.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.groupMembership.boxHeader()).to.eventually.eql('Group Membership Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'CREATE',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('Process Definition Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to process definition page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Process Definition');

      // then
      authorizationsPage.processDefinition.isActive();
      expect(authorizationsPage.processDefinition.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.processDefinition.boxHeader()).to.eventually.eql('Process Definition Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'READ',
          'CREATE_INSTANCE',
          'READ_INSTANCE',
          'UPDATE_INSTANCE',
          'DELETE_INSTANCE',
          'READ_TASK',
          'UPDATE_TASK',
          'READ_HISTORY',
          'DELETE_HISTORY'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });


    it('should create new authorization', function() {

      // when
      authorizationsPage.application.createNewAuthorization('ALLOW', 'GROUP', 'marketing', 'READ_INSTANCE', 'invoice');

      // then

    });

  });


  describe('Decision Definition Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to decision definition page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Decision Definition');

      // then
      authorizationsPage.decisionDefinition.isActive();
      expect(authorizationsPage.decisionDefinition.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.decisionDefinition.boxHeader()).to.eventually.eql('Decision Definition Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'READ',
          'CREATE_INSTANCE',
          'READ_HISTORY',
          'DELETE_HISTORY'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('Process Instance Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to process instance page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Process Instance');

      // then
      authorizationsPage.processInstance.isActive();
      expect(authorizationsPage.processInstance.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.processInstance.boxHeader()).to.eventually.eql('Process Instance Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'CREATE',
          'READ',
          'UPDATE',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('Task Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to task page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Task');

      // then
      authorizationsPage.task.isActive();
      expect(authorizationsPage.task.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.task.boxHeader()).to.eventually.eql('Task Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'CREATE',
          'READ',
          'UPDATE',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });


  describe('User Authorizations', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });

    it('should navigate to user page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('User');

      // then
      authorizationsPage.user.isActive();
      expect(authorizationsPage.user.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.user.boxHeader()).to.eventually.eql('User Authorizations');
    });


    it('should validate authorization attributes', function() {

      authorizationsPage.createNewButton().click().then(function() {

        checkCreateNewState();

        checkAuthorizationTypes();

        var permissionsList = [
          'READ',
          'UPDATE',
          'CREATE',
          'DELETE'
        ];
        checkPermissionTypes(permissionsList);

        abortCreatingNewAuthorization();
      });

    });

  });

  describe('New authorization on empty lists', function () {
    before(function () {
      return testHelper(setupFile.setup4, function () {
        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');

        authorizationsPage.selectAuthorizationNavbarItem('Task');
      });
    });

    it('can be created', function () {
      authorizationsPage.createNewButton().click().then(function() {
        checkCreateNewState();

        abortCreatingNewAuthorization();
      });
    });
  });

  describe('Update', function() {

    before(function() {
      return testHelper(setupFile.setup2, function() {
        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');

        authorizationsPage.selectAuthorizationNavbarItem('Task');
      });
    });

    it('can change user and group', function() {

      // when
      authorizationsPage.editButton(2).click();

      authorizationsPage.userGroupButton(2).click();
      authorizationsPage.userGroupInput(2).clear().sendKeys('a2_test');

      authorizationsPage.applyEditButton(2).click();

      // then
      expect(authorizationsPage.authorizationIdentityType(2)).to.eventually.eql('Group');
      expect(authorizationsPage.authorizationIdentity(2)).to.eventually.eql('a2_test');
    });


    it('can add a permission', function() {

      // when
      authorizationsPage.editButton(2).click();

      authorizationsPage.selectPermissionFor(2, 'UPDATE');

      authorizationsPage.applyEditButton(2).click();

      // then
      expect(authorizationsPage.authorizationPermissions(2)).to.eventually.eql('CREATE, UPDATE');
    });


    it('can change the resource id', function() {

      // when
      authorizationsPage.editButton(2).click();

      authorizationsPage.resourceInput(2).clear().sendKeys('foobar');

      authorizationsPage.applyEditButton(2).click();

      // then
      expect(authorizationsPage.authorizationResource(2)).to.eventually.eql('foobar');
    });


    it('should restore previous state on cancel', function() {

      // when
      authorizationsPage.editButton(3).click();

      authorizationsPage.userGroupInput(3).clear().sendKeys('a4');

      authorizationsPage.cancelEditButton(3).click();

      // then
      expect(authorizationsPage.authorizationIdentity(3)).to.eventually.eql('a3');
    });


    it('should not apply conflicting updates', function() {

      // when
      authorizationsPage.editButton(3).click();

      authorizationsPage.userGroupInput(3).clear().sendKeys('a4');

      authorizationsPage.applyEditButton(3).click();

      // need sleep because update is successful until the server says otherwise
      browser.sleep(500);

      // then
      expect(authorizationsPage.authorizationIdentity(3)).to.eventually.eql('a3');
    });
  });


  describe('Pagination', function () {

    before(function() {
    // create 45 authorizations... because we can.
      return testHelper(setupFile.setup3, function() {
        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');

        authorizationsPage.selectAuthorizationNavbarItem('Task');
      });
    });

    it('displays a pager', function () {

      // then
      expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
      expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(2);
    });

  });

});
