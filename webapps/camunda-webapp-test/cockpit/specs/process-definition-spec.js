/* jshint ignore:start */
'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/process-definition');


describe('Cockpit Process Definition Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });


    it('should go to process definition view', function() {

      // given
      var runningInstances = dashboardPage.deployedProcessesList.runningInstances(0);
      dashboardPage.deployedProcessesList.processName(0).then(function(processName) {

        // when
        dashboardPage.deployedProcessesList.selectProcess(0);

        // then
        expect(definitionPage.pageHeaderProcessDefinitionName()).to.eventually.eql(processName);
        expect(definitionPage.processInstancesTab.isTabSelected()).to.eventually.be.true;
        runningInstances.then(function(noOfInstances) {
          expect(definitionPage.processInstancesTab.table().count()).to.eventually.eql(parseInt(noOfInstances, 10));
        });
      });
    });


    it('should display definition key', function() {

      // then
      expect(definitionPage.filter.definitionKey()).to.eventually.contain('user-tasks');
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

  });


  describe('diagram interaction', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
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
      definitionPage.filter.removeSelectionButton('User Task 1').click();

      // then
      expect(definitionPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
    });

  });

});
