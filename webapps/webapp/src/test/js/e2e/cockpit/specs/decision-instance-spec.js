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
      });
    });

    it('should go to decision defintion view', function() {

      // given
      dashboardPage.deployedDecisionsList.decisionName(0).then(function(decisionName) {

        // when
        dashboardPage.deployedDecisionsList.selectDecision(0).then(function() {

          // then
          expect(definitionPage.pageHeader().getText()).to.eventually.match(new RegExp(decisionName));
          expect(definitionPage.pageHeader().getText()).to.eventually.match(/DECISION DEFINITION/);
        });
      })
    });


    it('should go to process instance view', function() {

      // given
      definitionPage.decisionInstancesTab.instanceId(0).then(function(instanceId) {

        // when
        definitionPage.decisionInstancesTab.selectInstanceId(0).then(function() {

          // then
          expect(instancePage.pageHeaderDecisionInstanceId()).to.eventually.match(new RegExp(instanceId));
        });
      });
    });

  });


  describe('table interaction', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should display decision table', function() {

      // when
      dashboardPage.deployedDecisionsList.selectDecision(0);
      definitionPage.decisionInstancesTab.selectInstanceId(0);

      // then
      expect(instancePage.table.tableElement().isDisplayed()).to.eventually.be.true;
    });

    it('should highlight fired rules', function() {
      expect(instancePage.table.row(0).getAttribute('class')).to.eventually.contain('fired');
    });

    it('should show the inputs on the table', function() {
      expect(instancePage.table.labelRow().getInputText(0)).to.eventually.eql('Invoice Amount = 100');
      expect(instancePage.table.labelRow().getInputText(1)).to.eventually.eql('Invoice Category = travelExpenses');
    });

    it('should show the outputs on the table', function() {
      expect(instancePage.table.ruleRow(0).getCellText(3)).to.eventually.eql('"accounting" = accounting');
    });

  });


  describe('in-/outputs', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should go to decision instance view', function() {

      // when
      dashboardPage.deployedDecisionsList.selectDecision(0);
      definitionPage.decisionInstancesTab.selectInstanceId(0);

      // then
      expect(instancePage.pageHeader().getText()).to.eventually.match(/DECISION INSTANCE/);
    });


    it('show inputs for the decision instance', function() {

      // when
      instancePage.inputsTab.selectTab();

      // then
      expect(instancePage.inputsTab.variableValue(0)).to.eventually.eql('100');
    });


    it('show outputs for the decision instance', function() {

      // when
      instancePage.outputsTab.selectTab();

      // then
      expect(instancePage.outputsTab.variableValue(0)).to.eventually.eql('accounting');
    });

  });


  describe('actions', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('go to decision instance view', function() {

      // when
      dashboardPage.deployedDecisionsList.selectDecision(0);
      definitionPage.decisionInstancesTab.selectInstanceId(0);
    });


    it('go to the process instance page', function() {

      // when
      instancePage.gotoProcessInstanceAction.gotoProcessInstance();

      // then
      expect(browser.getCurrentUrl()).to.eventually.contain('#/process-instance/');
    });

  });

});
