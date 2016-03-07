'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./task-dates-setup');

var dashboardPage = require('../pages/dashboard');
var taskViewPage = dashboardPage.currentTask;
var taskListPage = dashboardPage.taskList;


describe('Task Dates Spec', function() {

  describe('follow-up dates', function() {

    var followUpCreateTime;

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        taskListPage.selectTask('Task 1');
      });
    });

    it('should set follow up date to now', function() {

      // given
      expect(taskViewPage.followUpDate()).to.eventually.eql('Set follow-up date');

      // when
      taskViewPage.setFollowUpDate();
      followUpCreateTime = new Date();

      // then
      expect(taskViewPage.followUpDate()).to.eventually.eql('a few seconds ago');
    });


    it('should display the absolute date in the tooltip in the task detail view', function() {

      // then
      expect(taskViewPage.followUpDateTooltip()).to.eventually.include(followUpCreateTime.getDate());
      expect(taskViewPage.followUpDateTooltip()).to.eventually.include(followUpCreateTime.getFullYear());
    });


    it('should display relative follow up date in the list of tasks', function() {

      // then
      expect(taskListPage.taskFollowUpDate('Task 1')).to.eventually.include('a few seconds ago');
    });


    it('should display absolute follow up date in the tooltip in the list of tasks', function() {

      // then
      expect(taskListPage.taskFollowUpDateTooltip('Task 1')).to.eventually.include(followUpCreateTime.getDate());
      expect(taskListPage.taskFollowUpDateTooltip('Task 1')).to.eventually.include(followUpCreateTime.getFullYear());
    });


    it('should display follow up date in the tasks history', function() {

      // when
      taskViewPage.history.selectTab();

      // then
      expect(taskViewPage.history.eventType(0)).to.eventually.eql('Update');
      expect(taskViewPage.history.operationUser(0)).to.eventually.eql('admin');
      expect(taskViewPage.history.subEventType(0)).to.eventually.eql('Follow-up date');
      expect(taskViewPage.history.subEventNewValue(0, 0)).to.eventually.include(followUpCreateTime.getDate());
      expect(taskViewPage.history.subEventNewValue(0, 0)).to.eventually.include(followUpCreateTime.getFullYear());
    });


    it('should edit follow up date', function() {

      // when
      taskViewPage.setFollowUpDate('15:33');

      // then
      expect(taskViewPage.followUpDateTooltip()).to.eventually.include('15:33');
      expect(taskListPage.taskFollowUpDateTooltip('Task 1')).to.eventually.include('15:33');
    });


    it('should validate change in history tab', function() {

      // when
      taskViewPage.history.selectTab();

      // then
      expect(taskViewPage.history.eventType(0)).to.eventually.eql('Update');
      expect(taskViewPage.history.operationUser(0)).to.eventually.eql('admin');
      expect(taskViewPage.history.subEventType(0)).to.eventually.eql('Follow-up date');
      expect(taskViewPage.history.subEventNewValue(0, 0)).to.eventually.include('15:33');
    });

  });


  describe('due dates', function() {

    var dueDateCreateTime;

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        taskListPage.selectTask('Task 1');
      });
    });

    it('should set due date to now', function() {

      // given
      expect(taskViewPage.dueDateElement().getText()).to.eventually.eql('Set due date');

      // when
      taskViewPage.setDueDate();
      dueDateCreateTime = new Date();

      // then
      expect(taskViewPage.dueDate()).to.eventually.eql('a few seconds ago');
    });


    it('should display absolute due date in the tooltip in the task detail view', function() {

      // then
      expect(taskViewPage.dueDateTooltip()).to.eventually.include(dueDateCreateTime.getDate());
      expect(taskViewPage.dueDateTooltip()).to.eventually.include(dueDateCreateTime.getFullYear());
    });


    it('should display relative due date in the list of tasks', function() {

      // then
      expect(taskListPage.taskDueDate('Task 1')).to.eventually.include('a few seconds ago');
    });


    it('should display absolute due date in the tooltip in the list of tasks', function() {

      // then
      expect(taskListPage.taskDueDateTooltip('Task 1')).to.eventually.include(dueDateCreateTime.getDate());
      expect(taskListPage.taskDueDateTooltip('Task 1')).to.eventually.include(dueDateCreateTime.getFullYear());
    });


    it('should display due date in the tasks history', function() {

      // when
      taskViewPage.history.selectTab();

      // then
      expect(taskViewPage.history.eventType(0)).to.eventually.eql('Update');
      expect(taskViewPage.history.operationUser(0)).to.eventually.eql('admin');
      expect(taskViewPage.history.subEventType(0)).to.eventually.eql('Due date');
      expect(taskViewPage.history.subEventNewValue(0, 0)).to.eventually.include(dueDateCreateTime.getDate());
      expect(taskViewPage.history.subEventNewValue(0, 0)).to.eventually.include(dueDateCreateTime.getFullYear());
    });


    it('should edit due date', function() {

      // when
      taskViewPage.setDueDate('23:55');

      // then
      expect(taskViewPage.dueDateTooltip()).to.eventually.include('23:55');
      expect(taskListPage.taskDueDateTooltip('Task 1')).to.eventually.include('23:55');
    });


    it('should validate change in history tab', function() {

      // when
      taskViewPage.history.selectTab();

      // then
      expect(taskViewPage.history.eventType(0)).to.eventually.eql('Update');
      expect(taskViewPage.history.operationUser(0)).to.eventually.eql('admin');
      expect(taskViewPage.history.subEventType(0)).to.eventually.eql('Due date');
      expect(taskViewPage.history.subEventNewValue(0, 0)).to.eventually.include('23:55');
    });

  });

});
