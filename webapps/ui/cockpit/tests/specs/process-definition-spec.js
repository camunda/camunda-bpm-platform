/* jshint ignore:start */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var processesPage = require('../pages/processes');
var definitionPage = require('../pages/process-definition');


describe('Cockpit Process Definition Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
      });
    });


    it('should go to process definition view', function() {

      // given
      var runningInstances = processesPage.deployedProcessesList.runningInstances(0);
      processesPage.deployedProcessesList.processName(0).then(function(processName) {

        // when
        processesPage.deployedProcessesList.selectProcess(0);

        // then
        expect(definitionPage.pageHeaderProcessDefinitionName()).to.eventually.eql(processName);
        expect(definitionPage.processInstancesTab.isTabSelected()).to.eventually.be.true;
        runningInstances.then(function(noOfInstances) {
          expect(definitionPage.processInstancesTab.table().count()).to.eventually.eql(parseInt(noOfInstances, 10));
        });
      });
    });


    it('should display definition key', function() {
      // when
      definitionPage.sidebarTabClick('Information');

      // then
      expect(definitionPage.information.definitionKey()).to.eventually.contain('user-tasks');
    });


    it('should go to Called Process Definitions tab', function() {

      // when
      definitionPage.calledProcessDefinitionsTab.selectTab();

      // then
      expect(definitionPage.calledProcessDefinitionsTab.isTabSelected()).to.eventually.be.true;
      expect(definitionPage.calledProcessDefinitionsTab.tabName()).to.eventually.eql(definitionPage.calledProcessDefinitionsTab.tabLabel);
    });


    it('should go to Job Definitions tab', function() {

      // when
      definitionPage.jobDefinitionsTab.selectTab();

      // then
      expect(definitionPage.jobDefinitionsTab.isTabSelected()).to.eventually.be.true;
      expect(definitionPage.jobDefinitionsTab.tabName()).to.eventually.eql(definitionPage.jobDefinitionsTab.tabLabel);
    });


    it('should go to Process Instances tab', function() {

      // when
      definitionPage.processInstancesTab.selectTab();

      // then
      expect(definitionPage.processInstancesTab.isTabSelected()).to.eventually.be.true;
      expect(definitionPage.processInstancesTab.tabName()).to.eventually.eql(definitionPage.processInstancesTab.tabLabel);
    });

    it('should not display report link', function() {
      // then
      expect(definitionPage.getReportLink().isPresent()).to.eventually.be.false;
    });

  });


  describe('diagram interaction', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
      });
    });

    it('should display process diagram', function() {

      // then
      expect(definitionPage.diagram.diagramElement().isDisplayed()).to.eventually.be.true;
    });


    it('should display the number of running process instances', function() {

      // then
      expect(definitionPage.diagram.instancesBadgeFor('UserTask_1').getText()).to.eventually.eql('3');
    });


    it('should select activity', function() {

      // when
      definitionPage.diagram.selectActivity('UserTask_1');

      // then
      expect(definitionPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
      expect(definitionPage.filter.activitySelection('User Task 1').isPresent()).to.eventually.be.true;
    });


    it('should keep selection after page refresh', function() {

      // when
      browser.getCurrentUrl().then(function (url) {
        browser.get(url);
      });

      // then
      expect(definitionPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
    });


    it('should process clicks in Filter table', function() {

      // when
      definitionPage.sidebarTabClick('Filter');
      definitionPage.filter.removeSelectionButton('User Task 1').click();

      // then
      expect(definitionPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
    });

  });

  describe('multi tenancy', function() {

    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
      });
    });

    describe('process definition with tenant id', function() {

        before(function() {
          // first process definition is deployed for tenant with id 'tenant1'
          processesPage.deployedProcessesList.selectProcess(0);
        });

        it('should display definition tenant id', function() {
          // when
          definitionPage.sidebarTabClick('Information');

          // then
          expect(definitionPage.information.tenantId()).to.eventually.contain('tenant1');
        });

        it('should display definition version for tenant only', function() {
          // when
          definitionPage.sidebarTabClick('Information');

          // then
          expect(definitionPage.information.definitionVersion()).to.eventually.contain('1');
          expect(definitionPage.information.definitionVersionDropdownButton().isPresent()).to.eventually.be.false;
        });

        it('should display running instances for tenant only', function() {
          // when
          definitionPage.sidebarTabClick('Information');

          // then
          expect(definitionPage.information.definitionInstancesCurrent()).to.eventually.contain('1');
          expect(definitionPage.information.definitionInstancesAll()).to.eventually.contain('1');
        });

      });

    describe('process definition without tenant id', function() {

      before(function() {
        dashboardPage.navigateToWebapp('Cockpit');
        // second process definition is deployed without tenant id
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(1);
      });

      it('should not display definition tenant id', function() {
          // when
          definitionPage.sidebarTabClick('Information');

          // then
          expect(definitionPage.information.tenantId()).to.eventually.contain('null');
        });

      it('should display definition version for non-tenant only', function() {
        // when
        definitionPage.sidebarTabClick('Information');

        // then
        expect(definitionPage.information.definitionVersion()).to.eventually.contain('1');
        expect(definitionPage.information.definitionVersionDropdownButton().isPresent()).to.eventually.be.false;
      });

      it('should display running instances for non-tenant only', function() {
        // when
        definitionPage.sidebarTabClick('Information');

        // then
        expect(definitionPage.information.definitionInstancesCurrent()).to.eventually.contain('1');
        expect(definitionPage.information.definitionInstancesAll()).to.eventually.contain('1');
      });

    });

  });

});
