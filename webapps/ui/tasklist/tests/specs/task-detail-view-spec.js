'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./task-detail-view-setup');

var dashboardPage = require('../pages/dashboard');
var taskListPage = dashboardPage.taskList;
var taskViewPage = dashboardPage.currentTask;


describe('Tasklist Detail View Spec', function() {

  describe('the task detail view', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should display info text when no task is selected', function() {

      // then
      expect(taskViewPage.noTaskInfoText()).to.eventually.eql('Select a task in the list.');
    });


    it('should appear when a task is selected', function() {

      // when
      taskListPage.selectTask('Task 1');
      dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

      // then
      expect(taskViewPage.taskName()).to.eventually.eql('Task 1');
    });

    describe('description tab', function() {

      before(function() {
        taskListPage.selectTask('Task 1');
      });

      it('should display description of a task', function() {

        // given
        dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

        // when
        taskViewPage.description.selectTab();

        // then
        expect(taskViewPage.description.descriptionField()).to.eventually.eql(setupFile.setup1[1].params.description);
      });

    });


    describe('add a comment', function() {

      before(function() {
        taskListPage.selectTask('Task 1');
      });

      it('should display comment in the history tab', function() {

        // given
        var commentText = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris.' +
                           'Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor.' +
                           'Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim,' +
                           'ut porta lorem lacinia consectetur. Donec ut libero sed arcu vehicula ultricies a non tortor.' +
                           'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean ut gravida lorem. Ut turpis felis,' +
                           'pulvinar a semper sed, adipiscing id dolor. Pellentesque auctor nisi id magna consequat sagittis.' +
                           'Curabitur dapibus enim sit amet elit pharetra tincidunt feugiat nisl imperdiet. Ut convallis libero' +
                           'in urna ultrices accumsan. Donec sed odio eros. Donec viverra mi quis quam pulvinar ...';

        dashboardPage.waitForElementToBeVisible(taskViewPage.taskName(), 5000);

        // when
        taskViewPage.addComment(commentText);

        // then
        taskViewPage.history.selectTab();
        expect(taskViewPage.history.eventType(0)).to.eventually.eql('Comment');
        expect(taskViewPage.history.commentMessage(0)).to.eventually.eql(commentText);
        expect(taskViewPage.history.operationUser(0)).to.eventually.eql('admin');
      });

    });
  });


  describe('form tab', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    describe('with generic form and unclaimed task', function() {

      it('should disable form elements', function() {

        // given
        taskListPage.selectTask('Task 1');
        dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

        // when
        taskViewPage.form.selectTab();

        // then
        expect(taskViewPage.form.completeButton().isEnabled()).to.eventually.be.false;
      });

    });


    describe('with generic form and claimed task', function() {

      before(function() {
        return testHelper(setupFile.setup2, true);
      });

      it('should enable form elements', function() {
        // given
        taskListPage.selectTask('Task 1');
        dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

        // when
        taskViewPage.form.selectTab();

        // then
        expect(taskViewPage.form.completeButton().isEnabled()).to.eventually.be.true;
      });
    });

  });

  describe('form tab with failing form', function() {
    before(function() {
      return testHelper(setupFile.setup5, function() {
        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should enable form element after fail', function() {
      //given
      taskListPage.selectTask('Prevent_Task');
      dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());
      taskViewPage.form.selectTab();
      taskViewPage.claim();

      expect(taskViewPage.form.completeButton().isEnabled()).to.eventually.be.true;

      // when
      taskViewPage.form.completeButton().click();

      //then
      expect(taskViewPage.form.completeButton().isEnabled()).to.eventually.be.true;
    });
  });


  describe('bpmn diagram tab', function() {

    before(function() {
      return testHelper(setupFile.setup3, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should display the process and highlight current task', function() {

      // given
      taskListPage.selectTask('User Task 1');
      dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

      // when
      taskViewPage.diagram.selectTab();

      // then
      expect(taskViewPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
    });

  });

  describe('cmmn diagram tab', function() {

    before(function() {
      return testHelper(setupFile.setup4, function() {
        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should display the process and highlight current task', function() {
      // given
      taskListPage.selectTask('Task1');
      dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

      // when
      taskViewPage.diagram.selectTab();

      // then
      expect(taskViewPage.diagram.isActivitySelected('PlanItem_1')).to.eventually.be.true;
    });

    it('should display diagram and highlight current task after changing task', function() {
      // given
      taskListPage.selectTask('Task1');
      dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());
      taskViewPage.diagram.selectTab();

      // when
      taskListPage.selectTask('Task 2');

      // then
      expect(taskViewPage.diagram.isActivitySelected('PlanItem_2')).to.eventually.be.true;
    });
  });

  describe('multi tenancy', function() {

    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should display the tenant id', function() {

        // given
        taskListPage.selectTask('Task 1');
        dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

        // then
        expect(taskViewPage.taskTenantIdField().isPresent()).to.eventually.be.true;
        expect(taskViewPage.taskTenantIdField().getText()).to.eventually.eql('tenant1');
      });

    it('should not display the tenant id if not exist', function() {

      // given
      taskListPage.selectTask('Task 2');
      dashboardPage.waitForElementToBeVisible(taskViewPage.taskName());

      // then
      expect(taskViewPage.taskTenantIdField().isPresent()).to.eventually.be.false;
    });
  });

});
