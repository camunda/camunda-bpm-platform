'use strict';

var dashboardPage = require('../pages/dashboard');

describe('tasklist filter -', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      dashboardPage.navigateTo();
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      dashboardPage.isActive();
    });

  });


  xdescribe('create new filter', function() {

    it('should open create new filter page', function () {

      // when
      dashboardPage.taskFilters.createFilterButton().click();


      dashboardPage.taskFilters.createFilterPage.nameInput('MY OWN TASKS FILTER');
      dashboardPage.taskFilters.createFilterPage.priorityInput().clear();
      dashboardPage.taskFilters.createFilterPage.priorityInput(-2000);
      dashboardPage.taskFilters.createFilterPage.descriptionInput('test filter for testing purpose');

      dashboardPage.taskFilters.editFilterPage.selectPanel('Criteria');
      dashboardPage.taskFilters.editFilterPage.addCriteria('Key', 'invoice');
      dashboardPage.taskFilters.editFilterPage.addCriteria('Assignee', '${currentUser()}');

      dashboardPage.taskFilters.createFilterPage.saveButton().click();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).toBe('MY OWN TASKS FILTER');
      dashboardPage.taskFilters.isFilterSelected(1);
    });

  });


  describe('comments', function() {

    it('should select task', function () {

      // when
      dashboardPage.taskFilters.selectFilter(0);
      dashboardPage.taskList.selectTask(0);

      // then
      expect(dashboardPage.currentTask.addCommentButton().isEnabled()).toBe(true);
    });


    it('should add new comment', function () {

      // when
      dashboardPage.currentTask.addComment('Alles scheiße!!!');
      dashboardPage.currentTask.selectTab(1);

      expect(dashboardPage.currentTask.history.historyList().count()).toBe(1);
      expect(dashboardPage.currentTask.history.getHistoryEventType(0)).toBe('Comment');
      expect(dashboardPage.currentTask.history.getHistoryCommentMessage(0)).toBe('Alles scheiße!!!');
      expect(dashboardPage.currentTask.history.getHistoryOperationUser(0)).toBe('jonny1');
    });


    it('should unclaim task', function () {

      // when
      dashboardPage.currentTask.unclaim();
      dashboardPage.currentTask.selectTab(1);

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(0);
      expect(dashboardPage.currentTask.history.historyList().count()).toBe(2);
      expect(dashboardPage.currentTask.history.getHistoryEventType(0)).toBe('Assign');
      expect(dashboardPage.currentTask.history.getHistoryAssignee(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.getHistoryOperationUser(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.getHistoryCommentMessage(1)).toBe('Alles scheiße!!!');
    });

    it('should claim task', function () {

      // when
      dashboardPage.currentTask.claim();
      dashboardPage.currentTask.selectTab(1);

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(1);
      expect(dashboardPage.currentTask.history.historyList().count()).toBe(3);
      expect(dashboardPage.currentTask.history.getHistoryEventType(0)).toBe('Claim');
      expect(dashboardPage.currentTask.history.getHistoryClaimee(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.getHistoryOperationUser(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.getHistoryCommentMessage(2)).toBe('Alles scheiße!!!');
    });

  });



  xdescribe('end test', function() {

    it('should delete filter', function () {

      // when
      dashboardPage.taskFilters.deleteFilter(0);
      dashboardPage.taskFilters.deleteFilterPage.deleteButton().click();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).not.toBe('MY OWN TASKS FILTER');
    });


    it('should logout', function() {

      dashboardPage.navigateLogout();
    });

  });

});