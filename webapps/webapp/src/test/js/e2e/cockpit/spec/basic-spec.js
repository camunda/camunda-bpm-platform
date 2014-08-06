'use strict';

var dashboardPage = require('../pages/dashboard');
var processDefinitionPage = require('../pages/process-definition');

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

});