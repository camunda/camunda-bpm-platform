'use strict';

var testSetup = require('../../spec-setup');
var setupFile = require('./admin-user-setup');
testSetup(setupFile);

var usersPage = require('../pages/users');

describe('Admin - admin setup -', function() {

  it('should validate admin setup page', function () {

    console.log('\n' + 'admin-user-spec');

    // when
    usersPage.navigateToWebapp('Admin');

    // then
    expect(usersPage.adminUserSetup.pageHeader()).toBe('Setup');
    expect(usersPage.adminUserSetup.createNewAdminButton().isEnabled()).toBe(false);
  });


  it('should enter new admin profile', function() {

    // when
    usersPage.adminUserSetup.userIdInput('Admin');
    usersPage.adminUserSetup.passwordInput('admin123');
    usersPage.adminUserSetup.passwordRepeatInput('admin123');
    usersPage.adminUserSetup.userFirstNameInput('Über');
    usersPage.adminUserSetup.userLastNameInput('Admin');
    usersPage.adminUserSetup.userEmailInput('uea@camundo.org');

    usersPage.adminUserSetup.createNewAdminButton().click();

    // then
    expect(usersPage.adminUserSetup.statusMessage()).toBe('User created You have created an initial user.');
  });


  it('should login as new Admin', function() {

    // when
    usersPage.navigateToWebapp('Admin');
    usersPage.authentication.userLogin('Admin', 'admin123');

    // then
    expect(usersPage.userFirstNameAndLastName(0)).toBe('Über Admin');
  });

});
