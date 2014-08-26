'use strict';

var dashboardPage = require('../pages/dashboard');
var processDefinitionPage = require('../pages/process-definition');
var processInstancePage = require('../pages/process-instance');

describe('cockpit dashboard - ', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

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
      processDefinitionPage.table.calledProcessDefinitionsTab.selectTab();

      // then
      expect(processDefinitionPage.table.calledProcessDefinitionsTab.tabName()).toBe('Called Process Definitions');
      processDefinitionPage.table.calledProcessDefinitionsTab.isTabSelected();
    });


    it('should check Job Definitions tabs', function() {

      // when
      processDefinitionPage.table.jobDefinitionsTab.selectTab();

      // then
      expect(processDefinitionPage.table.jobDefinitionsTab.tabName()).toBe('Job Definitions');
      processDefinitionPage.table.jobDefinitionsTab.isTabSelected();
    });


    it('should check Process Instances tabs', function() {

      // when
      processDefinitionPage.table.processInstancesTab.selectTab();

      // then
      expect(processDefinitionPage.table.processInstancesTab.tabName()).toBe('Process Instances');
      processDefinitionPage.table.processInstancesTab.isTabSelected();
    });


    function clickFirstProcessInstance() {
      return processDefinitionPage.table.processInstancesTab.processInstanceName(0).then(function(title) {
        processDefinitionPage.table.processInstancesTab.selectProcessInstance(0);
        return title;
      });
    }

    function getPageHeaderId() {
      return processInstancePage.pageHeaderProcessInstanceName().then(function(id) {
        return id.replace('<', '').replace('>', '');
      });
    }

    it('should select process instance and check instance view page', function() {

      // when
      var instanceId;

      clickFirstProcessInstance()
        .then(function(id) {
          instanceId = id;
        })
        .then(getPageHeaderId)
        .then(function(headerId) {
          expect(instanceId).toBe(headerId);
        })
        .then(function() {
          processInstancePage.isActive({ instance: instanceId });
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