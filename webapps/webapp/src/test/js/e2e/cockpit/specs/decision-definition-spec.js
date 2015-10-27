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
      dashboardPage.deployedDecisionsList.decisionName(0).then(function(decisionName) {

        // when
        dashboardPage.deployedDecisionsList.selectDecision(0);

        // then
        expect(definitionPage.pageHeaderDecisionDefinitionName()).to.eventually.eql(decisionName);

      });
    });

  });

  describe('instance list', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedDecisionsList.selectDecision(0);
      });
    });

    it('should display a list of evaluated decision instances', function() {
      expect(definitionPage.decisionInstancesTab.isTabSelected()).to.eventually.be.true;
      expect(definitionPage.decisionInstancesTab.table().count()).to.eventually.eql(1);
    });

    it('should go to the process definition page on click on process definition key', function() {
      definitionPage.decisionInstancesTab.selectProcessDefinitionKey(0);
      expect(browser.getCurrentUrl()).to.eventually.contain('#/process-definition/');
    });

    it('should go to the process instance page on click on process instance id', function() {
      browser.navigate().back();
      definitionPage.decisionInstancesTab.selectProcessInstanceId(0);
      expect(browser.getCurrentUrl()).to.eventually.contain('#/process-instance/');
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

  describe('version interaction', function() {
    before(function() {
      return testHelper(setupFile.setup2, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedDecisionsList.selectDecision(0);
      });
    });

    it('should display the most recent version initially', function() {
      expect(definitionPage.version.getVersion()).to.eventually.eql('2');
    });

    it('should list all available versions', function() {
      // when
      definitionPage.version.getDropdownButton().click();

      // then
      expect(definitionPage.version.getDropdownOptions().count()).to.eventually.eql(2);
    });

    it('should load the requested version on selection', function() {
      // when
      definitionPage.version.getDropdownOption(1).click();

      // then
      expect(definitionPage.version.getVersion()).to.eventually.eql('1');
    });

  });

});
