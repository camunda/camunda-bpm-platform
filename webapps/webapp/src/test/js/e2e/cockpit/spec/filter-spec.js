'use strict';

var dashboardPage = require('../pages/dashboard');
var processDefinitionPage = require('../pages/process-definition');

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


  xdescribe('add variables to a process', function () {

  });


  xdescribe('filter by variable', function () {

  });


  describe('filter by start date', function () {

    it('should select first process', function () {

      // given
      dashboardPage.clickNavBarHeader();

      // when
      dashboardPage.deployedProcessesList.selectProcess(4);
    });


    it('should add new filter', function () {

      // when
      processDefinitionPage.filter.addFilterByVariable('doubleVar = 6123.2025');

      browser.sleep(5000);

      // then
      expect(processDefinitionPage.table.processInstancesTab.table().count()).toBe(5);

    });

  });


  xdescribe('combine filter', function () {

  });


  describe('end test', function() {

    it('should log out', function () {

      dashboardPage.logout();
    });

  });


});