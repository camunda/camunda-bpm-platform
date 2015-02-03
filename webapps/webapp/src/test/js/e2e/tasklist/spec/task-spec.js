'use strict';

var dashboardPage = require('../pages/dashboard');

describe('tasklist filter -', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      dashboardPage.navigateToWebapp('Tasklist');
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');
    });

  });


  describe('create new filter', function() {

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
      dashboardPage.currentTask.addComment('Thiß üs ä cömmänt');
      dashboardPage.currentTask.history.selectTab();

      expect(dashboardPage.currentTask.history.historyList().count()).toBe(1);
      expect(dashboardPage.currentTask.history.eventType(0)).toBe('Comment');
      expect(dashboardPage.currentTask.history.commentMessage(0)).toBe('Thiß üs ä cömmänt');
      expect(dashboardPage.currentTask.history.operationUser(0)).toBe('jonny1');
    });


    it('should unclaim task', function () {

      // when
      dashboardPage.currentTask.unclaim();
      dashboardPage.currentTask.history.selectTab();

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(0);
      expect(dashboardPage.currentTask.history.historyList().count()).toBe(2);
      expect(dashboardPage.currentTask.history.eventType(0)).toBe('Assign');
      expect(dashboardPage.currentTask.history.subEventType(0)).toBe('Assignee');
      expect(dashboardPage.currentTask.history.operationUser(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.subEventOriginalValue(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.commentMessage(1)).toBe('Thiß üs ä cömmänt');
    });


    it('should claim task', function () {

      // when
      dashboardPage.currentTask.claim();
      dashboardPage.currentTask.history.selectTab();

      // then
      expect(dashboardPage.taskList.taskList().count()).toBe(1);
      expect(dashboardPage.currentTask.history.historyList().count()).toBe(3);
      expect(dashboardPage.currentTask.history.eventType(0)).toBe('Claim');
      expect(dashboardPage.currentTask.history.subEventNewValue(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.operationUser(0)).toBe('jonny1');
      expect(dashboardPage.currentTask.history.commentMessage(2)).toBe('Thiß üs ä cömmänt');
    });


    describe('dates', function () {

      it('should set follow-up date', function () {

        // when
        dashboardPage.currentTask.setFollowUpDate();

        // then
        expect(dashboardPage.currentTask.history.eventType(0)).toBe('Update');
        expect(dashboardPage.currentTask.history.operationUser(0)).toBe('jonny1');
        expect(dashboardPage.currentTask.history.subEventType(0)).toBe('Follow-up date');

        dashboardPage.currentTask.history.operationTime(0).then(function(eventTime) {
          expect(dashboardPage.currentTask.history.subEventNewValue(0)).toContain(eventTime);
        });
        expect(dashboardPage.currentTask.followUpDateText()).toBe('a few seconds ago');
      });


      it('should set due date', function () {

        // when
        dashboardPage.currentTask.setDueDate();

        // then
        expect(dashboardPage.currentTask.history.eventType(0)).toBe('Update');
        expect(dashboardPage.currentTask.history.operationUser(0)).toBe('jonny1');
        expect(dashboardPage.currentTask.history.subEventType(0)).toBe('Due date');

        dashboardPage.currentTask.history.operationTime(0).then(function(eventTime) {
          expect(dashboardPage.currentTask.history.subEventNewValue(0)).toContain(eventTime);
        });
        expect(dashboardPage.currentTask.dueDateText()).toBe('a few seconds ago');
      });

    });

  });


  describe('end test', function() {

    it('should delete filter', function () {

      // when
      dashboardPage.taskFilters.editFilter(0);
      dashboardPage.taskFilters.editFilterPage.deleteFilterButton().click();
      dashboardPage.taskFilters.deleteFilterPage.deleteButton().click();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).not.toBe('MY OWN TASKS FILTER');
    });


    it('should logout', function() {

      dashboardPage.navigateLogout();
    });

  });

});
