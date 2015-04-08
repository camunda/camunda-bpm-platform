/* jshint ignore:start */
'use strict';

var fs = require('fs');

var testHelper = require('../../test-helper');
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/process-definition');
var instancePage = require('../pages/process-instance');

describe('Cockpit Process Instance Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });


    it('should go to process instance view', function() {

      // given
      definitionPage.processInstancesTab.instanceId(1).then(function(instanceIdy) {

        // when
        definitionPage.processInstancesTab.selectInstance(1);

        // then
        expect(instancePage.pageHeaderProcessInstanceName()).to.eventually.eql(instanceIdy);
      });
    });


    it('should go to User Tasks tab', function() {

      // when
      instancePage.userTasksTab.selectTab();

      // then
      expect(instancePage.userTasksTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.userTasksTab.tabName()).to.eventually.eql(instancePage.userTasksTab.tabLabel)
    });


    it('should go to Called Process Instances tab', function() {

      // when
      instancePage.calledInstancesTab.selectTab();

      // then
      expect(instancePage.calledInstancesTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.calledInstancesTab.tabName()).to.eventually.eql(instancePage.calledInstancesTab.tabLabel)
    });


    it('should go to Incidents tab', function() {

      // when
      instancePage.incidentsTab.selectTab();

      // then
      expect(instancePage.incidentsTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.incidentsTab.tabName()).to.eventually.eql(instancePage.incidentsTab.tabLabel)
    });


    it('should go to Variables tab', function() {

      // when
      instancePage.variablesTab.selectTab();

      // then
      expect(instancePage.variablesTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.variablesTab.tabName()).to.eventually.eql(instancePage.variablesTab.tabLabel)
    });

  });


  describe('work with process variables', function() {

    before(function() {
      return testHelper(setupFile, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstance(0);
      });
    });


    it('should add process variable', function() {

      // given
      expect(instancePage.variablesTab.table().count()).to.eventually.eql(3);

      // when
      instancePage.actionBar.addVariable('myTestVar', 'String', '12345');

      // then
      expect(instancePage.variablesTab.table().count()).to.eventually.eql(4);
      expect(instancePage.variablesTab.variableName(3)).to.eventually.eql('myTestVar');
      expect(instancePage.variablesTab.variableType(3)).to.eventually.eql('String');
      expect(instancePage.variablesTab.variableValue(3)).to.eventually.eql('12345');
      expect(instancePage.variablesTab.variableScopeName(3)).to.eventually.eql('User Tasks');
    });


    it('should change variable', function() {

      // given
      expect(instancePage.variablesTab.variableType(2)).to.eventually.eql('Double');
      expect(instancePage.variablesTab.variableValue(2)).to.eventually.eql('1.49');

      // when
      instancePage.variablesTab.editVariableButton(2).click().then(function() {
        instancePage.variablesTab.editVariableValue().clear();
        instancePage.variablesTab.editVariableValue('1.5');
        instancePage.variablesTab.editVariableType('String');
        instancePage.variablesTab.editVariableConfirmButton().click();
      });

      // then
      expect(instancePage.variablesTab.variableName(2)).to.eventually.eql('test');
      expect(instancePage.variablesTab.variableValue(2)).to.eventually.eql('1.5');
      expect(instancePage.variablesTab.variableType(2)).to.eventually.eql('String');
    });


    it('should select wrong variable type', function() {

      // given
      expect(instancePage.variablesTab.variableType(1)).to.eventually.eql('Date');
      expect(instancePage.variablesTab.variableValue(1)).to.eventually.eql('2011-11-11T11:11:11');

      // when
      instancePage.variablesTab.editVariableButton(1).click().then(function() {
        instancePage.variablesTab.editVariableType('Short');
      });

      // then
      expect(instancePage.variablesTab.editVariableErrorText()).to.eventually.eql('Invalid value: Only a Short value is allowed.');
      expect(instancePage.variablesTab.editVariableConfirmButton().isEnabled()).to.eventually.be.false;

      // finaly
      instancePage.variablesTab.editVariableCancelButton().click().then(function() {
        expect(instancePage.variablesTab.inlineEditRow().isPresent()).to.eventually.be.false;
      });

    });

  });


  describe('diagram interaction', function() {

    before(function() {
      return testHelper(setupFile, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstance(0);
      });
    });


    it('should display process diagram', function() {
      expect(instancePage.diagram.diagramElement().isDisplayed()).to.eventually.be.true;
    });


    it('should select unselectable task', function() {

      // when
      instancePage.diagram.selectActivity('UserTask_2');

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_2')).to.eventually.be.false;
    });


    it('should display the number of concurrent activities', function() {
      expect(instancePage.diagram.instancesBadgeFor('UserTask_1').getText()).to.eventually.eql('1');
    });


    it('should process clicks in diagram', function() {

      // given
      instancePage.instanceTree.selectInstance('User Task 1');
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;

      // when
      instancePage.diagram.deselectAll();

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
      expect(instancePage.instanceTree.isInstanceSelected('User Task 1')).to.eventually.be.false;
    });


    it('should keep selection after page refresh', function() {

      // given
      instancePage.instanceTree.selectInstance('User Task 1');
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;

      // when
      browser.getCurrentUrl().then(function(url) {
        browser.get(url).then(function() {
          browser.sleep(500);
        });
      });

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
    });


    it('should reflect the tree view selection in diagram', function() {

      // given
      instancePage.instanceTree.selectInstance('User Task 1');
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;

      // when
      instancePage.instanceTree.deselectInstance('User Task 1');

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
    });

  });

});
