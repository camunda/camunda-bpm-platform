/* jshint ignore:start */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./decision-setup');

var dashboardPage = require('../pages/dashboard');
var decisionsPage = require('../pages/decisions');
var definitionPage = require('../pages/decision-definition');


describe('Cockpit Decision Definition Spec', function() {

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
        decisionsPage.deployedDecisionsList.selectDecision(0);

        // then
        expect(definitionPage.information.definitionName()).to.eventually.eql(decisionName);

      });
    });

  });

  describe('instance list', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Decisions');
        decisionsPage.deployedDecisionsList.selectDecision(0);
      });
    });

    it('should display a list of evaluated decision instances', function() {
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
        dashboardPage.goToSection('Decisions');
        decisionsPage.deployedDecisionsList.selectDecision(0);
      });
    });

    it('should display decision table', function() {
      // then
      expect(definitionPage.table.tableElement().isDisplayed()).to.eventually.be.true;
    });
  });

  describe('search widget', function() {

    before(function() {
      return testHelper(setupFile.setup5, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Decisions');
        decisionsPage.deployedDecisionsList.selectDecision(0);
      });
    });

    afterEach(function() {
      definitionPage.search.clearSearch();
    });

    it('should add new filter', function() {
      //when
      definitionPage.search.createSearch('ID', '1.5');

      //then
      var searchPillsCount = definitionPage
        .search
        .searchPills()
        .count();

      expect(searchPillsCount).to.eventually.eql(1);
    });

    it('should empty table when non existing value is chosen', function() {
      //when
      definitionPage.search.createSearch('ID', 'ff');

      //then
      var tableRowsCount = definitionPage
        .decisionInstancesTab
        .table()
        .all(by.css('tbody > tr'))
        .count();

      expect(tableRowsCount).to.eventually.eql(0);
    });

    it('should have after and before operators for Evaluated Date filter', function() {
      //given
      definitionPage
        .search
        .createSearch('Evaluated Date');

      //when
      definitionPage
        .search
        .searchPills()
        .last()
        .element(by.css('[value="operator.value"]'))
        .click();

      //then
      expect(isOperatorDisplayed('after')).to.eventually.be.true;
      expect(isOperatorDisplayed('before')).to.eventually.be.true;

      function isOperatorDisplayed(operator) {
        return definitionPage
          .search
          .searchPills()
          .last()
          .element(by.cssContainingText('[value="operator.value"] .dropdown-menu li', operator))
          .isDisplayed();
      }
    });
  });

  describe('version interaction', function() {
    before(function() {
      return testHelper(setupFile.setup2, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Decisions');
        decisionsPage.deployedDecisionsList.selectDecision(0);
      });
    });

    it('should display the most recent version initially', function() {
      expect(definitionPage.information.definitionVersionDropdownButtonText()).to.eventually.eql('2');
    });

    it('should list all available versions', function() {
      // when
      definitionPage.information.definitionVersionDropdownButton().click();

      // then
      expect(definitionPage.information.definitionVersionDropdownOptions().count()).to.eventually.eql(2);
    });

    it('should load the requested version on selection', function() {
      // when
      definitionPage.information.definitionVersionDropdownOptions().get(0).click();

      // then
      expect(definitionPage.information.definitionVersionDropdownButtonText()).to.eventually.eql('2');
    });

  });

  describe('multi tenancy', function() {

    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Decisions');
      });
    });

    describe('decision definition with tenant id', function() {

        before(function() {
          // second decision definition is deployed for tenant with id 'tenant1'
          decisionsPage.deployedDecisionsList.selectDecision(1);
        });

        it('should display definition tenant id', function() {

          expect(definitionPage.information.tenantId()).to.eventually.contain('tenant1');
        });

        it('should display definition version for tenant only', function() {

          expect(definitionPage.information.definitionVersion()).to.eventually.contain('1');
          expect(definitionPage.information.definitionVersionDropdownButton().isPresent()).to.eventually.be.false;
        });

      });

    describe('decision definition without tenant id', function() {

      before(function() {
        dashboardPage.navigateToWebapp('Cockpit');
        // first decision definition is deployed without tenant id
        dashboardPage.goToSection('Decisions');
        decisionsPage.deployedDecisionsList.selectDecision(0);
      });

      it('should not display definition tenant id', function() {

        expect(definitionPage.information.tenantId()).to.eventually.contain('null');
      });

      it('should display definition version for non-tenant only', function() {

        expect(definitionPage.information.definitionVersion()).to.eventually.contain('1');
        expect(definitionPage.information.definitionVersionDropdownButton().isPresent()).to.eventually.be.false;
      });

    });

  });

});
