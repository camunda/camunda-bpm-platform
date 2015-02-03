'use strict';

var dashboardPage = require('../pages/dashboard');
var cockpitPage = require('../../cockpit/pages/dashboard');
var cockpitProcessDefinitionPage = require('../../cockpit/pages/process-definition');
var cockpitProcessInstancePage = require('../../cockpit/pages/process-instance');

describe('tasklist dashboard - ', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      dashboardPage.navigateTo();
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');
    });

  });


  describe('claim and unclaim', function () {

    it('should claim a task', function () {

      // given
      dashboardPage.taskFilters.selectFilter(3); // select ALL TASK filter

      // when
      var taskName = dashboardPage.taskList.taskName(0);
      dashboardPage.taskList.selectTask(0);
      dashboardPage.currentTask.claim();
      dashboardPage.taskFilters.selectFilter(0); // select MY TASKS filter

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(2);
      expect(dashboardPage.taskList.taskName(0)).toEqual(taskName);
    });


    it('should unclaim a task', function () {

      // when
      dashboardPage.taskList.selectTask(0);
      dashboardPage.currentTask.unclaim();

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(1);
    });

  });


  describe('filter list', function() {

    it('should select MY TASKS filter', function() {

      // when
      dashboardPage.taskFilters.selectFilter(0);

      // then
      dashboardPage.taskFilters.isFilterSelected(0);
      dashboardPage.taskFilters.isFilterNotSelected(1);
      expect(dashboardPage.taskList.taskList().count()).toBe(1);
    });


    describe('Start a process', function() {

      xit('should search process', function() {

        // when
        dashboardPage.selectNavbarItem('Process');
        dashboardPage.startProcess.searchProcessInput().sendKeys('invoice receipt');

        // then
        expect(dashboardPage.startProcess.foundProcesses().count()).toBe(2);
      });


      xit('should start process', function() {

        // when
        dashboardPage.startProcess.selectProcessFromSearchResult(1);

        // then
        expect(dashboardPage.startProcess.invoiceStartForm.creditorInput().isDisplayed()).toBe(true);
      });


      it('should start process', function () {

        // when
        dashboardPage.selectNavbarItem('Process');
        dashboardPage.startProcess.selectProcess(12);

        // then
        expect(dashboardPage.startProcess.invoiceStartForm.creditorInput().isDisplayed()).toBe(true);
      });


      it('should enter start form data', function() {

        dashboardPage.startProcess.invoiceStartForm.creditorInput().sendKeys('Bier');
        dashboardPage.startProcess.invoiceStartForm.amountInput().sendKeys('85746€');
        dashboardPage.startProcess.invoiceStartForm.invoiceNumberInput().sendKeys('4711');
        dashboardPage.startProcess.invoiceStartForm.startButton().click();
      });


      it('should check filter refresh', function() {

        // then
        expect(dashboardPage.taskList.taskList().count()).toBe(2);
      });

    });

  });


  describe('work on task', function() {

    it('should select a task', function() {

      // when
      dashboardPage.taskFilters.selectFilter(0);
      dashboardPage.taskList.selectTask(1);

      // then
      expect(dashboardPage.currentTask.taskName()).toBe('Assign Approver');
      expect(dashboardPage.currentTask.processName()).toBe('invoice receipt');
    });

    it('should enter my task data', function() {

      // when
      dashboardPage.startProcess.invoiceStartForm.approverInput().sendKeys('jonny1');
      dashboardPage.currentTask.completeButton().click();

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(1);
      expect(dashboardPage.taskList.taskName(0)).not.toBe('Approve Invoice');
    });

  });


  xdescribe('end test', function() {

    it('should logout', function() {

  /*    cockpitPage.navigateTo();
      cockpitPage.deployedProcessesList.selectProcess(12);
      cockpitProcessDefinitionPage.table.processInstancesTab.selectProcessInstance(0);
      cockpitProcessInstancePage.actionButton.cancelInstance();*/

    });

  });

});
