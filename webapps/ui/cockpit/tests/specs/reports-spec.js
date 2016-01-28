'use strict';

var testHelper = require('../../../common/tests/test-helper');

var reportsPage = require('../pages/reports');


describe('Cockpit Reports Spec', function() {

  describe('reports page', function() {

    before(function() {
      return testHelper([], function() {
        reportsPage.navigateToWebapp('Cockpit');
        reportsPage.authentication.userLogin('admin', 'admin');
        reportsPage.navigateTo();
      });

    });

    it('shows that no report is available', function() {

      // then
      reportsPage.isActive();
      expect(reportsPage.noReportsAvailableHint().isPresent()).to.eventually.be.true;
    });

  });

});
