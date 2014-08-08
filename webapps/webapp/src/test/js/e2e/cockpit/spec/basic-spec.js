'use strict';

var dashboardPage = require('../pages/dashboard');
var processDefinitionPage = require('../pages/process-definition');
var processInstancePage = require('../pages/process-instance');

describe('cockpit dashboard - ', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.login('jonny1', 'jonny1');

      // then
      dashboardPage.isActive();
    });

  });


  describe('dashboard', function () {

    it('should select process', function() {

      // when
      dashboardPage.deployedProcessesList.selectProcess(0);

      // then
      expect(processDefinitionPage.pageHeaderProcessDefinitionName()).toEqual('PROCESS DEFINITION\n' + 'Another Failing Process');
    });

  });


  describe('process definition view', function() {

    it('should select diagram element', function() {

      // when
      processDefinitionPage.diagram.selectActivity('service.task.1');

      // then
      processDefinitionPage.diagram.isActivitySelected('service.task.1');
    });


    it('should deselect diagram element', function() {

      // when
      processDefinitionPage.diagram.deselectActivity('service.task.1');

      // then
      processDefinitionPage.diagram.isActivityNotSelected('service.task.1');
    });


    it('should check Called Process Definitions tabs', function() {

      // when
      processDefinitionPage.table.calledProcessDefinitionsTab.selectCalledProcessDefinitionsTab();

      // then
      expect(processDefinitionPage.table.calledProcessDefinitionsTab.calledProcessDefinitionsTabName()).toBe('Called Process Definitions');
      processDefinitionPage.table.calledProcessDefinitionsTab.isCalledProcessDefinitionsTabSelected();
    });


    it('should check Process Instances tabs', function() {

      // when
      processDefinitionPage.table.processInstancesTab.selectProcessInstanceTab();

      // then
      expect(processDefinitionPage.table.processInstancesTab.processInstanceTabName()).toBe('Process Instances');
      processDefinitionPage.table.processInstancesTab.isProcessInstanceTabSelected();
    });


    it('should select process instance and check instance view page', function() {

      // when
      var tableInstanceId = processDefinitionPage.table.processInstancesTab.processInstanceName(0);
      processDefinitionPage.table.processInstancesTab.selectProcessInstance(0);

      // then
      var tableInstanceIdString;
      var instanceViewInstanceIdString;
      tableInstanceId.then(function(instanceIDString) {
        tableInstanceIdString = instanceIDString;
      });

      processInstancePage.pageHeaderProcessInstanceName().then(function(instanceViewInstanceId) {
        instanceViewInstanceId = instanceViewInstanceId.replace('<', '');
        instanceViewInstanceId = instanceViewInstanceId.replace('>', '');
        instanceViewInstanceIdString = instanceViewInstanceId;
      }).then(function() {
        expect(instanceViewInstanceIdString).toBe(tableInstanceIdString);
        processInstancePage.isActive({ instance: tableInstanceIdString});
      });

    });

  });


  describe('process instance view', function() {

    it('should select diagram element', function() {

      // when
      processInstancePage.diagram.selectActivity('service.task.1');

      // then
      processInstancePage.diagram.isActivitySelected('service.task.1');
    });


    it('should deselect diagram element', function() {

      // when
      processInstancePage.diagram.deselectActivity('service.task.1');

      // then
      processInstancePage.diagram.isActivityNotSelected('service.task.1');
    });

  });

});