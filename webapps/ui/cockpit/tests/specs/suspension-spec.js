'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./suspension-setup');

var dashboardPage = require('../pages/dashboard');
var processesPage = require('../pages/processes');
var definitionPage = require('../pages/process-definition');
var instancePage = require('../pages/process-instance');


describe('Cockpit Suspsension Spec', function() {

  describe('process definition suspension', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
      });
    });

    it('should suspend definition immediately', function() {

      // when
      definitionPage.suspension.suspendDefinition();

      // then
      expect(definitionPage.isDefinitionSuspended()).to.eventually.be.true;
    });


    it('should active definition immediately', function() {

      // when
      definitionPage.suspension.activateDefinition();

      // then
      expect(definitionPage.isDefinitionSuspended()).to.eventually.be.false;
    });

  });


  describe('process instance suspension', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstanceId(0);
      });
    });

    it('should suspend instance', function() {

      // when
      instancePage.suspension.suspendInstance();

      // then
      expect(instancePage.isInstanceSuspended()).to.eventually.be.true;
    });


    it('should validate suspended instance', function() {

      // when
      instancePage.navbarBrand().click();
      dashboardPage.goToSection('Processes');
      processesPage.deployedProcessesList.selectProcess(0);

      // then
      expect(definitionPage.processInstancesTab.isInstanceSuspended(0)).to.eventually.be.true;

      // finaly
      definitionPage.processInstancesTab.selectInstanceId(0);
    });


    it('should active instance', function() {

      // when
      instancePage.suspension.activateInstance();

      // then
      expect(instancePage.isInstanceSuspended()).to.eventually.be.false;
    });

  });


  describe('job suspension', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
        definitionPage.jobDefinitionsTab.selectTab();
      });
    });

    it('should suspend job definition immediately', function() {

      // when
      definitionPage.jobDefinitionsTab.suspendJobDefinition(0);

      // then
      expect(definitionPage.diagram.isActivitySuspended('HelloCallActivity')).to.eventually.be.true;
    });


    it('should active job definition immediately', function() {

      // when
      definitionPage.jobDefinitionsTab.activateJobDefinition(0);

      // then
      expect(definitionPage.diagram.isActivitySuspended('HelloCallActivity')).to.eventually.be.false;
    });

    it('should display suspension badge on suspension for second job for activity', function() {

      // when
      definitionPage.jobDefinitionsTab.suspendJobDefinition(1);

      // then
      expect(definitionPage.diagram.isActivitySuspended('HelloCallActivity')).to.eventually.be.true;
    });

  });

});
