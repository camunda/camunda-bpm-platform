/* jshint ignore:start */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./decision-setup');

var dashboardPage = require('../pages/dashboard');
var decisionsPage = require('../pages/decisions');
var definitionPage = require('../pages/decision-definition');
var instancePage = require('../pages/decision-instance');


describe('Cockpit Decision Instance Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Decisions');
      });
    });

    it('should go to decision definition view', function() {

      // given
      decisionsPage.deployedDecisionsList.decisionName(0).then(function(decisionName) {

        // when
        decisionsPage.deployedDecisionsList.selectDecision(0).then(function() {

          // then
          expect(dashboardPage.breadcrumb.activeCrumb().getText()).to.eventually.eql(decisionName);
        });
      });
    });


    it('should go to process instance view', function() {

      // given
      definitionPage.decisionInstancesTab.instanceId(0).then(function(instanceId) {

        // when
        definitionPage.decisionInstancesTab.selectInstanceId(0).then(function() {

          // then
          expect(instancePage.information.instanceId()).to.eventually.eql(instanceId);
          expect(dashboardPage.breadcrumb.activeCrumb().getText()).to.eventually.eql(instanceId);
        });
      });
    });

  });


  describe('table interaction', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Decisions');
      });
    });

    it('should display decision table', function() {

      // when
      decisionsPage.deployedDecisionsList.selectDecision(0);
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
        dashboardPage.goToSection('Decisions');
      });
    });

    it('should go to decision instance view', function() {
      // when
      decisionsPage.deployedDecisionsList.selectDecision(0);

      definitionPage.decisionInstancesTab.instanceId(0).then(function(instanceId) {
        definitionPage.decisionInstancesTab.selectInstanceId(0);

        // then
        expect(dashboardPage.breadcrumb.activeCrumb().getText()).to.eventually.eql(instanceId);
      });
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


  describe('in-/outputs without clause name', function() {

    before(function() {
      return testHelper(setupFile.setup3, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Decisions');
      });
    });

    it('should go to decision instance view', function() {
      // when
      decisionsPage.deployedDecisionsList.selectDecision(0);

      definitionPage.decisionInstancesTab.instanceId(0).then(function(instanceId) {
        definitionPage.decisionInstancesTab.selectInstanceId(0);

        // then
        expect(dashboardPage.breadcrumb.activeCrumb().getText()).to.eventually.eql(instanceId);
      });
    });


    it('show inputs clause id', function() {

      // when
      instancePage.inputsTab.selectTab();

      // then
      expect(instancePage.inputsTab.variableName(0)).to.eventually.eql('clause1');
    });


    it('show outputs clause id', function() {

      // when
      instancePage.outputsTab.selectTab();

      // then
      expect(instancePage.outputsTab.variableName(0)).to.eventually.eql('clause3');
    });

  });


  describe('actions', function() {

    describe('navigate to process instance', function() {

      before(function() {
        return testHelper(setupFile.setup1, function() {

          dashboardPage.navigateToWebapp('Cockpit');
          dashboardPage.authentication.userLogin('admin', 'admin');
          dashboardPage.goToSection('Decisions');
        });
      });

      it('go to decision instance view', function() {

        // when
        decisionsPage.deployedDecisionsList.selectDecision(0);
        definitionPage.decisionInstancesTab.selectInstanceId(0);
      });


      it('go to the process instance page', function() {

        // when
        instancePage.gotoProcessInstance();

        // then
        expect(browser.getCurrentUrl()).to.eventually.contain('#/process-instance/');
      });

    });


    describe('navigation action', function() {

      before(function() {
        return testHelper(setupFile.setup4, function() {

          dashboardPage.navigateToWebapp('Cockpit');
          dashboardPage.authentication.userLogin('admin', 'admin');
          dashboardPage.goToSection('Decisions');
          decisionsPage.deployedDecisionsList.selectDecision(0);
          definitionPage.decisionInstancesTab.selectInstanceId(0);
        });
      });

      it('is not displayed', function() {

        // then
        expect(instancePage.processInstanceLink().getText()).to.eventually.eql('null');
      });

    });

  });


  describe('multi tenancy', function() {

    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should not display tenant id of instance if not exists', function() {

      // given
      dashboardPage.goToSection('Decisions');

      // when
      decisionsPage.deployedDecisionsList.selectDecision(0);
      definitionPage.decisionInstancesTab.selectInstanceId(0);

      // then
      expect(instancePage.information.tenantId()).to.eventually.contain('null');
    });


    it('should display tenant id of instance', function() {

      // given
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.goToSection('Decisions');

      // when
      decisionsPage.deployedDecisionsList.selectDecision(1);
      definitionPage.decisionInstancesTab.selectInstanceId(0);

      // then
      expect(instancePage.information.tenantId()).to.eventually.contain('tenant1');
    });

  });

});
