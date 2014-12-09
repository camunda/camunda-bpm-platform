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


  describe('add variables to a process', function () {

    it('should select process', function () {

      // given
      dashboardPage.clickNavBarHeader();

      // when
      dashboardPage.deployedProcessesList.selectProcess(4);

      // then
      processDefinitionPage.table.processInstancesTab.selectProcessInstance(0);
    });


    it('should add new integer variable to instance', function () {

      // given
      processInstancePage.table.variablesTab.table().count().then(function(variableCount) {

        // when
        processInstancePage.actionBar.addVariable('normInt', 'Integer', 36999);

        // then
        expect(processInstancePage.table.variablesTab.table().count()).toBeGreaterThan(variableCount);
      });

    });

    // disabled due to CAM-3177
    xit('should add new double variable to instance', function () {

      // given
      processInstancePage.table.variablesTab.table().count().then(function(variableCount) {

        // when
        processInstancePage.actionBar.addVariable('fatDouble', 'Double', 1234567890987654321);

        // then
        expect(processInstancePage.table.variablesTab.table().count()).toBeGreaterThan(variableCount);
      });

    });

    // disabled due to CAM-3177
    xit('should add new date variable to instance', function () {

      // given
      processInstancePage.table.variablesTab.table().count().then(function(variableCount) {

        // when
        processInstancePage.actionBar.addVariable('dateAlaaf', 'Date', '2014-11-11T11:11:00');

        // then
        expect(processInstancePage.table.variablesTab.table().count()).toBeGreaterThan(variableCount);
      });

    });


    it('should navigate to process defintion view', function () {

      processInstancePage.selectBreadCrumb(1);
    });

  });


  describe('filter by variable', function () {

    it('should select process', function () {

      // given
      dashboardPage.clickNavBarHeader();

      // when
      dashboardPage.deployedProcessesList.selectProcess(4);
    });


    it('should add new filter', function () {

      // when
      processDefinitionPage.filter.addFilterByVariable('normInt = 36999');

      browser.sleep(5000);

      // then
      expect(processDefinitionPage.table.processInstancesTab.table().count()).toBe(1);

    });

  });


  xdescribe('filter by start date', function () {

  });


  xdescribe('combine filter', function () {

  });


  describe('end test', function() {

    it('should log out', function () {

      dashboardPage.logout();
    });

  });


});