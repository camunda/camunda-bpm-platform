/* jshint ignore:start */
'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./decision-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/decision-definition');


describe('Cockpit Decision Definition Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });


    it('should go to decision definition view', function() {

      // given
      //var runningInstances = dashboardPage.deployedProcessesList.runningInstances(0);
      dashboardPage.deployedDecisionsList.decisionName(0).then(function(decisionName) {

        // when
        dashboardPage.deployedDecisionsList.selectDecision(0);

        // then
        expect(definitionPage.pageHeaderDecisionDefinitionName()).to.eventually.eql(decisionName);

      });
    });

  });


  describe('table interaction', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedDecisionsList.selectDecision(0);
      });
    });

    it('should display decision table', function() {

      // then
      expect(definitionPage.table.tableElement().isDisplayed()).to.eventually.be.true;
    });

  });

});
