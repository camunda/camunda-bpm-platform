'use strict';

var testHelper = require('../../test-helper');
var dashboardPage = require('../pages/dashboard');


describe('Tasklist app Spec', function() {
  before(function () {
    return testHelper(function() {
      dashboardPage.navigateToWebapp('Tasklist');
    });
  });


  describe('anonymous user', function () {
    it('does not show the user menu', function () {
      expect(dashboardPage.accountDropdown().isPresent()).to.eventually.eql(false);
    });
  });


  describe('authenticated user', function () {
    before(function () {
      dashboardPage.authentication.userLogin('admin', 'admin');
    });

    it('shows a user menu', function () {
      expect(dashboardPage.accountDropdown().isPresent()).to.eventually.eql(true);
    });

    it('shows the full name of user', function () {
      expect(dashboardPage.accountDropdownButton().getText()).to.eventually.eql('Steve Hentschi');
    });
  });
});
