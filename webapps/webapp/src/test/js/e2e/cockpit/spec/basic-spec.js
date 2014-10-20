//  - Login Page
//        |
//        v  login
//  - Dashboard View
//        |
//        v  select Process Defintion
//  - Process Defintion View
//    - Called Process Definitions tab
//        |
//        v  open Called Process
//    - Called Process Definitions tab
//        |
//        v  switch tab
//    - Job Definitions tabs
//        |
//        v  switch tab
//    - Process Instances tabs
//        |
//        v  select instance
//  - Process Instance View

'use strict';

var dashboardPage = require('../pages/dashboard');
var processDefinitionPage = require('../pages/process-definition');
var processInstancePage = require('../pages/process-instance');

describe('cockpit - ', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      dashboardPage.isActive();
      expect(dashboardPage.navBarHeaderName()).toBe('camunda Cockpit');
    });

  });


  describe('dashboard', function () {

    it('should select process', function() {

      // when
      dashboardPage.deployedProcessesList.selectProcess(3);

      // then
      expect(processDefinitionPage.fullPageHeaderProcessDefinitionName()).toEqual('PROCESS DEFINITION\n' + 'CallingCallActivity');
    });

  });


  describe('process definition view', function() {

    it('should check Called Process Definitions tab', function() {

      // when
      processDefinitionPage.table.calledProcessDefinitionsTab.selectTab();

      // then
      expect(processDefinitionPage.table.calledProcessDefinitionsTab.tabName()).toBe('Called Process Definitions');
      processDefinitionPage.table.calledProcessDefinitionsTab.isTabSelected();
    });


    function clickFirstCalledProceesDefintion() {
      return processDefinitionPage.table.calledProcessDefinitionsTab.calledProcessDefintionName(0).then(function(title) {
        processDefinitionPage.table.calledProcessDefinitionsTab.selectCalledProcessDefinitions(0);
        return title;
      });
    }

    it("should open called process defintion", function () {

      var processName;

      // when
      clickFirstCalledProceesDefintion().then(function(name) {
        processName = name;
      })
      .then(processDefinitionPage.pageHeaderProcessDefinitionName)
      .then(function(headerName) {

        // then
        expect(processName).toBe(headerName);
      });
      // check URL
    });


    it("should select calling activity", function () {

      // when
      processDefinitionPage.table.calledProcessDefinitionsTab.selectTab();
      processDefinitionPage.table.calledProcessDefinitionsTab.selectCalledFromActivity(0);

      // then
      processInstancePage.diagram.isActivitySelected('CallActivity_1');
    });
    
    
    it("should go deeper", function () {

      // when
      processDefinitionPage.table.calledProcessDefinitionsTab.selectCalledProcessDefinitions(0);

      // then
      expect(processDefinitionPage.pageHeaderProcessDefinitionName()).toBe('FailingProcess');
    });


    it('should check Job Definitions tab', function() {

      // when
      processDefinitionPage.table.jobDefinitionsTab.selectTab();

      // then
      expect(processDefinitionPage.table.jobDefinitionsTab.tabName()).toBe('Job Definitions');
      processDefinitionPage.table.jobDefinitionsTab.isTabSelected();
      processDefinitionPage.table.calledProcessDefinitionsTab.isTabNotSelected();
    });


    it("should select job activity", function () {

      // when
      processDefinitionPage.table.jobDefinitionsTab.selectJobDefinition(0);

      // then
      processInstancePage.diagram.isActivitySelected('ServiceTask_1');
      expect(processDefinitionPage.table.jobDefinitionsTab.jobDefinitionName(0)).toBe('Service Task');
      expect(processDefinitionPage.table.jobDefinitionsTab.suspendJobButton(0).isEnabled()).toBe(true);
    });


    it('should check Process Instances tab', function() {

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

    it('should select process instance and check instance view page', function() {

      // when
      var instanceId;

      clickFirstProcessInstance()
        .then(function(id) {
          instanceId = id;
        })
        .then(processInstancePage.pageHeaderProcessInstanceName)
        .then(function(headerId) {
          expect(instanceId).toBe(headerId);
        })
        .then(function() {
          processInstancePage.isActive({ instance: instanceId });
        });

    });

  });


  describe("cockpit navigation", function () {

    it("should call dashboard by bread crumb", function () {

      processInstancePage.pageHeaderProcessInstanceName()
          .then(function(instanceId) {

            // when
            processInstancePage.selectBreadCrumb(0);

            // then
            expect(dashboardPage.isActive());

            // when
            browser.navigate().back();

            // then
            processInstancePage.isActive({ instance: instanceId });
          })
    });


    it("should call defintions page by bread crumb", function () {

      // when
      processInstancePage.selectBreadCrumb(2);

      // then
      processDefinitionPage.table.processInstancesTab.selectProcessInstance(5);
    });


    it("should call dashboard by header menu", function () {
      var url;

      browser.getCurrentUrl()
        .then(function(urlString) {
          url = urlString;
        })
        .then(processInstancePage.pageHeaderProcessInstanceName)
        .then(function(instanceId) {

          // when
          processInstancePage.clickNavBarHeader();

          // then
          dashboardPage.isActive();

          // when
          browser.get(url);

          // then
          processInstancePage.isActive({ instance: instanceId });
        });

    });

  }); 


  describe('process instance view', function() {

    it('should select diagram element', function() {

      // when
      processInstancePage.diagram.selectActivity('ServiceTask_1');

      // then
      processInstancePage.diagram.isActivitySelected('ServiceTask_1');
    });


    it('should deselect diagram element', function() {

      // when
      processInstancePage.diagram.deselectActivity('ServiceTask_1');

      // then
      processInstancePage.diagram.isActivityNotSelected('ServiceTask_1');
    });

  });

  xdescribe("action button toolbar", function () {

    it("should validate action buttons", function () {
      expect(processInstancePage.actionButton.actionButtonList().count()).toBe(4);

    });

    it("should cancel process", function () {

      // when
      processInstancePage.actionButton.cancelInstance();



    });

    it("should prompt ok", function () {

    });

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

  });

});