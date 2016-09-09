'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./users-setup');

var users = setupFile.setup1;

testHelper.expectStringEqual = require('../../../common/tests/string-equal');

var usersPage = require('../pages/users');
var groupsPage = require('../pages/groups');


describe('Admin Users Spec', function() {

  describe('user page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
      });
    });

    beforeEach(function() {
      usersPage.navigateTo();
    });

    it('should validate users page', function() {

      // when
      usersPage.selectNavbarItem('Users');

      // then
      usersPage.isActive();
      testHelper.expectStringEqual(usersPage.pageHeader(), 'Users');
      expect(usersPage.newUserButton().isEnabled()).to.eventually.eql(true);
    });


    it('should select user name in list', function() {

      // when
      usersPage.selectUserByNameLink(2);

      // then
      usersPage.editUserProfile.isActive({ user: users[1].params.id });
      testHelper.expectStringEqual(usersPage.editUserProfile.pageHeader(), users[1].params.firstName + ' ' + users[1].params.lastName);
      expect(usersPage.editUserProfile.emailInput().getAttribute('value')).to.eventually.eql(users[1].params.email);
    });


    it('should select edit link in list', function() {

      // when
      usersPage.selectUserByEditLink(1);

      // then
      usersPage.editUserProfile.isActive({ user: users[0].params.id });
      testHelper.expectStringEqual(usersPage.editUserProfile.pageHeader(), users[0].params.firstName + ' ' + users[0].params.lastName);
      expect(usersPage.editUserProfile.emailInput().getAttribute('value')).to.eventually.eql(users[0].params.email);
    });

  });

  describe('create a new user', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should open Create New User page', function() {

      // given
      usersPage.navigateTo();

      // when
      usersPage.newUserButton().click();

      // then
      usersPage.newUser.isActive();
      testHelper.expectStringEqual(usersPage.newUser.pageHeader(), 'Create');
      expect(usersPage.newUser.createNewUserButton().isEnabled()).to.eventually.eql(false);
    });


    it('should enter new user data', function() {

      // when
      usersPage.newUser.createNewUser('Xäbi', 'pass', 'pass', 'Xäbi', 'Älönsö', 'xaebi.aeloensoe@fcb.de' );
      usersPage.editUserProfile.navigateTo({ user: 'Xäbi' });

      // then
      testHelper.expectStringEqual(usersPage.editUserProfile.pageHeader(), 'Xäbi Älönsö');
    });


    it('should count users', function() {

      // when
      usersPage.navigateTo();

      // then
      expect(usersPage.userList().count()).to.eventually.eql(5);
    });


    it('should login with new account',function() {

      // given
      usersPage.logout();
      usersPage.navigateToWebapp('Welcome');

      // when
      usersPage.authentication.userLogin('Xäbi', 'pass');
      browser.get(usersPage.url);

      // then
      expect(usersPage.loggedInUser()).to.eventually.eql('Xäbi Älönsö');
      expect(usersPage.userList().count()).to.eventually.eql(1);
    });

  });


  describe('delete a user', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
        browser.get(usersPage.url);
      });
    });

    it('should navigate to Account menu', function() {

      // when
      usersPage.selectUser(1);
      usersPage.editUserAccount.selectUserNavbarItem('Account');

      // then
      expect(usersPage.editUserAccount.subHeaderDeleteUser()).to.eventually.eql('Delete User');
    });


    it('should delete user', function() {

      // given
      var userName = usersPage.editUserAccount.pageHeader();

      // when
      usersPage.editUserAccount.deleteUser();

      // then
      expect(usersPage.userList().count()).to.eventually.eql(3);
      expect(usersPage.userFirstNameAndLastName(1)).to.not.eventually.eql(userName);
    });

  });


  describe('edit a user', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
        browser.get(usersPage.url);
      });
    });

    describe('profile', function() {

      it('should validate profile page', function() {

        // when
        usersPage.selectUser(1);
        usersPage.editUserProfile.selectUserNavbarItem('Profile');

        // then
        expect(usersPage.editUserProfile.subHeader()).to.eventually.include('Profile');
        expect(usersPage.editUserProfile.updateProfileButton().isEnabled()).to.eventually.eql(false);
      });


      it('should update profile', function() {

        // when
        usersPage.editUserProfile.firstNameInput('i');
        usersPage.editUserProfile.updateProfileButton().click();

        // then
        testHelper.expectStringEqual(usersPage.editUserProfile.pageHeader(),
          users[0].params.firstName + 'i' + ' ' + users[0].params.lastName);
      });

    });


    describe('account', function() {

      beforeEach(function() {
        usersPage.editUserAccount.navigateTo({ user: users[2].params.id });
      });

      it('should validate account page', function() {

        // when
        usersPage.editUserAccount.selectUserNavbarItem('Account');

        // then
        expect(usersPage.editUserAccount.subHeaderChangePassword()).to.eventually.eql('Change Password');
        expect(usersPage.editUserAccount.changePasswordButton().isEnabled()).to.eventually.eql(false);
        expect(usersPage.editUserAccount.deleteUserButton().isEnabled()).to.eventually.eql(true);
      });


      it('should not change password due to wrong myPassword', function() {

        // when
        usersPage.editUserAccount.changePassword('wrongMyPassword', 'newPassword123', 'newPassword123');

        // then
        expect(usersPage.editUserAccount.notification()).to.eventually.eql('Your password is not valid.');
      });


      it('should not enable button due to wrong password repetition)', function() {

        // when
        usersPage.editUserAccount.myPasswordInput('admin');
        usersPage.editUserAccount.newPasswordInput('asdfasdf');
        usersPage.editUserAccount.newPasswordRepeatInput('asdfasdg');

        // then
        expect(usersPage.editUserAccount.changePasswordButton().isEnabled()).to.eventually.eql(false);
      });


      it('should change password', function() {

        // when
        usersPage.editUserAccount.myPasswordInput('admin');
        usersPage.editUserAccount.newPasswordInput('asdfasdf');
        usersPage.editUserAccount.newPasswordRepeatInput('asdfasdf');

        // then
        usersPage.editUserAccount.changePasswordButton().click();
      });


      it('should validate new password', function() {

        // given
        usersPage.logout();
        usersPage.navigateToWebapp('Welcome');

        // when
        usersPage.authentication.userLogin(users[2].params.id, 'asdfasdf');
        browser.get(usersPage.url);

        // then
        expect(usersPage.userFirstNameAndLastName(0)).to.eventually.eql(users[2].params.firstName + ' ' + users[2].params.lastName);
      });

    });

  });


  describe('add/delete group', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
        browser.get(usersPage.url);
      });
    });

    it('should navigate to groups menu', function() {

      // when
      usersPage.selectUserByEditLink(3);
      usersPage.editUserProfile.selectUserNavbarItem('Groups');

      // then
      usersPage.editUserGroups.isActive({ user: users[2].params.id });
      expect(usersPage.editUserGroups.subHeader()).to.eventually
        .eql(users[2].params.firstName + ' ' + users[2].params.lastName + '\'s' + ' ' + 'Groups');
      expect(usersPage.editUserGroups.groupId(0)).to.eventually.eql('accounting');
      expect(usersPage.editUserGroups.groupList().count()).to.eventually.eql(1);
    });


    it('should add new group - select group modal', function() {

      // when
      usersPage.editUserGroups.openAddGroupModal();

      //then
      expect(usersPage.editUserGroups.selectGroupModal.pageHeader()).to.eventually.eql('Select Groups');
      expect(usersPage.editUserGroups.selectGroupModal.groupList().count()).to.eventually.eql(2);
    });


    it('should add new group', function() {

      // when
      usersPage.editUserGroups.selectGroupModal.addGroup(1);

      // then
      expect(usersPage.editUserGroups.groupId(1)).to.eventually.eql('sales');
      expect(usersPage.editUserGroups.groupList().count()).to.eventually.eql(2);
    });


    it('should remove group', function() {

      // when
      usersPage.editUserGroups.removeGroup(0);

      // then
      expect(usersPage.editUserGroups.groupId(0)).to.eventually.eql('sales');
      expect(usersPage.editUserGroups.groupList().count()).to.eventually.eql(1);
    });


    it('should navigate to groups edit menu', function() {

      // given
      usersPage.editUserGroups.navigateTo({ user: users[2].params.id });
      usersPage.editUserGroups.openAddGroupModal();


      // when
      usersPage.editUserGroups.selectGroupModal.groupName(0).getText().then(function(groupName) {
        browser.sleep(300).then(function() {
          usersPage.editUserGroups.selectGroupModal.groupId(0).click().then(function() {

            // then
            testHelper.expectStringEqual(groupsPage.editGroup.pageHeader(), groupName);
          });
        });
      });

    });


    describe('use special group names', function() {

      before(function() {
        return testHelper(setupFile.setup2, function() {

          usersPage.navigateToWebapp('Admin');
          usersPage.authentication.userLogin('admin', 'admin');
          browser.get(usersPage.url);
        });
      });

      it('should validate group selection modal', function() {

        // given
        usersPage.editUserGroups.navigateTo({ user: users[2].params.id });

        // when
        usersPage.editUserGroups.openAddGroupModal();

        // then
        expect(usersPage.editUserGroups.selectGroupModal.groupId(0).getText()).to.eventually.eql('/göäüp_name');
        expect(usersPage.editUserGroups.selectGroupModal.groupId(1).getText()).to.eventually.eql('\\göäüp_name');
      });


      it('should add slash group', function() {

        // when
        usersPage.editUserGroups.selectGroupModal.addGroup(0);

        // then
        expect(usersPage.editUserGroups.groupId(0)).to.eventually.eql('/göäüp_name');
        expect(usersPage.editUserGroups.groupList().count()).to.eventually.eql(2);
      });


      it('should open group selection modal and validate the rest', function() {

        // given
        usersPage.editUserGroups.navigateTo({ user: users[2].params.id });

        // when
        usersPage.editUserGroups.openAddGroupModal();

        // then
        expect(usersPage.editUserGroups.selectGroupModal.groupId(0).getText()).to.eventually.eql('\\göäüp_name');
      });


      it('should add backslash group', function() {

        // when
        usersPage.editUserGroups.selectGroupModal.addGroup(0);

        // then
        expect(usersPage.editUserGroups.groupId(1)).to.eventually.eql('\\göäüp_name');
        expect(usersPage.editUserGroups.groupList().count()).to.eventually.eql(3);
      });

    });

  });

  describe('User Tenants', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should navigate to tenants menu', function() {
      // given
      usersPage.navigateTo();

      // when
      usersPage.selectUserByEditLink(3);
      usersPage.editUserProfile.selectUserNavbarItem('Tenants');

      // then
      usersPage.editUserTenants.isActive({ user: users[2].params.id });
      expect(usersPage.editUserTenants.subHeader()).to.eventually
        .eql(users[2].params.firstName + ' ' + users[2].params.lastName + '\'s' + ' ' + 'Tenants');
      expect(usersPage.editUserTenants.tenantList().count()).to.eventually.eql(0);
    });

    it('should add user to tenant - select tenant modal', function() {

      // when
      usersPage.editUserTenants.openAddTenantModal();

      //then
      expect(usersPage.editUserTenants.selectTenantModal.pageHeader()).to.eventually.eql('Select Tenants');
      expect(usersPage.editUserTenants.selectTenantModal.tenantList().count()).to.eventually.eql(2);
    });

    it('should add user to tenants', function() {
      // when
      usersPage.editUserTenants.selectTenantModal.addTenants();

      // then
      expect(usersPage.editUserTenants.tenantId(0)).to.eventually.eql('tenantOne');
      expect(usersPage.editUserTenants.tenantList().count()).to.eventually.eql(2);
    });

    it('should show tenants', function() {
      // given
      usersPage.navigateTo();

      // when
      usersPage.selectUserByEditLink(3);
      usersPage.editUserProfile.selectUserNavbarItem('Tenants');

      // then
      usersPage.editUserTenants.isActive({ user: users[2].params.id });
      expect(usersPage.editUserTenants.tenantList().count()).to.eventually.eql(2);
    });

    it('should remove tenant', function() {
      // when
      usersPage.editUserTenants.removeTenant(0);

      expect(usersPage.editUserTenants.tenantList().count()).to.eventually.eql(1);
    });
  });

  describe('Pagination', function () {

    before(function() {
      return testHelper(setupFile.setup3, function() {
        usersPage.navigateToWebapp('Admin');
        usersPage.authentication.userLogin('admin', 'admin');
        browser.get(usersPage.url);
      });
    });

    it('displays a pager', function () {

      // then
      expect(element(by.css('.pagination')).isPresent()).to.eventually.eql(true);
      expect(element.all(by.css('[ng-repeat="page in pages track by $index"]')).count()).to.eventually.eql(2);
    });

  });

});
