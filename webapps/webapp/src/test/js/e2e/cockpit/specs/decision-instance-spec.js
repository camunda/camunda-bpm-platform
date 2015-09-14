/* jshint ignore:start */
'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./decision-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/decision-definition');
var instancePage = require('../pages/decision-instance');


describe('Cockpit Decision Instance Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedDecisionsList.selectDecision(0);
      });
    });

    it('should go to process instance view', function() {

      // given
      definitionPage.decisionInstancesTab.instanceId(0).then(function(instanceId) {

        // when
        definitionPage.decisionInstancesTab.selectInstanceId(0);

        // then
        expect(instancePage.pageHeaderDecisionInstanceId()).to.eventually.eql(instanceId);
      });
    });

  });

  describe('table interaction', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedDecisionsList.selectDecision(0);
        definitionPage.decisionInstancesTab.selectInstanceId(0);
      });
    });

    it('should display decision table', function() {
      // then
      expect(instancePage.table.tableElement().isDisplayed()).to.eventually.be.true;
    });

  });

  describe('in-/outputs', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedDecisionsList.selectDecision(0);
        definitionPage.decisionInstancesTab.selectInstanceId(0);
      });
    });

    it('show inputs for the decision instance', function() {
      instancePage.inputsTab.selectTab();

      expect(instancePage.inputsTab.variableValue(0)).to.eventually.eql('100');
    });

    it('show outputs for the decision instance', function() {
      instancePage.outputsTab.selectTab();

      expect(instancePage.outputsTab.variableValue(0)).to.eventually.eql('accounting');
    });
  });

  describe('actions', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedDecisionsList.selectDecision(0);
        definitionPage.decisionInstancesTab.selectInstanceId(0);
      });
    });

    it('go to the process instance page', function() {
      instancePage.gotoProcessInstanceAction.gotoProcessInstance();
      expect(browser.getCurrentUrl()).to.eventually.contain('#/process-instance/');
    });

  });

});
