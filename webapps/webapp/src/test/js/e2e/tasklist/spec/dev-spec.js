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


  describe("claim and unclaim", function () {

    it("should count tasks", function () {

      dashboardPage.taskFilters.selectFilter(0);

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(2);
    });

    it("should name task", function () {

      dashboardPage.taskList.selectTask(1);

      expect(dashboardPage.taskList.taskName(1)).toBe('Horst');
      expect(dashboardPage.taskList.taskProcessDefinitionName(1)).toBe('Hrubesch');
    });

    it("should change assignee", function () {
      dashboardPage.taskFilters.selectFilter(2);
      expect(dashboardPage.taskList.taskList().count()).toBe(2);

      dashboardPage.taskList.selectTask(1);
      dashboardPage.currentTask.editClaimedUser('Puups');

      browser.sleep(5000);
    });

    it("should select tabs", function () {
      dashboardPage.currentTask.setDueDate();
      browser.sleep(2000);
    });

  });

});