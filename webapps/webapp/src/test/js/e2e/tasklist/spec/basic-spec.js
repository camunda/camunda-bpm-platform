'use strict';

var dashboardPage = require('../pages/dashboard');

describe('tasklist dashboard - ', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      dashboardPage.navigateTo();
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      dashboardPage.isActive();
    });

  });


  describe('filter list', function() {

    it('should select MINES filter', function() {

      // when
      dashboardPage.filter.selectFilter(1);

      // then
      dashboardPage.filter.isFilterSelected(1);
      dashboardPage.filter.isFilterNotSelected(0);
      expect(dashboardPage.tasks.taskList().count()).toBe(1);
    });


    describe('Start a process', function() {

      it('should search and start process', function() {

        // when
        dashboardPage.selectNavbarItem('Process');
        dashboardPage.startProcess.searchProcessInput().sendKeys('invoice receipt');

        // then
        expect(dashboardPage.startProcess.foundProcesses().count()).toBe(2);
      });


      it('should start process', function() {

        // when
        dashboardPage.startProcess.selectProcessFromSearchResult(1);

        // then
        expect(dashboardPage.startProcess.invoiceStartForm.creditorInput().isDisplayed()).toBe(true);
      });


      it('should enter start form data', function() {

        dashboardPage.startProcess.invoiceStartForm.creditorInput().sendKeys('Bier');
        dashboardPage.startProcess.invoiceStartForm.amountInput().sendKeys('85746€');
        dashboardPage.startProcess.invoiceStartForm.invoiceNumberInput().sendKeys('4711');
        dashboardPage.startProcess.invoiceStartForm.startButton().click();
      });

    });


    it('should check filter refresh', function() {

      // then
      expect(dashboardPage.tasks.taskList().count()).toBe(2);
    });


  });


  xdescribe('tasks', function() {

    it('should open my tasks', function() {

      // when
      dashboardPage.filter.selectFilter(1);

      // then
      expect(dashboardPage.tasks.taskList().count()).toBe(1);
    });


    it('should select a task', function() {

      // when
      dashboardPage.tasks.selectTask(0);

      // then
      expect(dashboardPage.task.taskName()).toBe('Assign Approver');
      expect(dashboardPage.task.processName()).toBe('invoice receipt');
    });

    it('should enter my task data', function() {

      // when
      dashboardPage.startProcess.invoiceStartForm.approverInput().sendKeys('jonny1');
      dashboardPage.task.completeButton().click();


      // then
      expect(dashboardPage.tasks.taskList().count()).toBe(1);
      expect(dashboardPage.tasks.taskTitle(0)).not.toBe('Approve Invoice');
    });

  });


  xdescribe('end test', function() {

    it('should logout', function() {


    });

  });

});