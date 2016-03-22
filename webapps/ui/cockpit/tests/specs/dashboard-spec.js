'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./dashboard-setup');

var dashboardPage = require('../pages/dashboard');


describe('Cockpit Dashboard Spec', function() {
  before(function() {
    return testHelper(setupFile.setup1, function() {
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.authentication.userLogin('admin', 'admin');
    });
  });

  it('should display the sections on the page', function () {
    expect(dashboardPage.pluginList().count()).to.eventually.eql(4);
  });

  it('should display the sections in the header navigation', function () {
    expect(dashboardPage.navbarItems().count()).to.eventually.eql(4);
  });
});
