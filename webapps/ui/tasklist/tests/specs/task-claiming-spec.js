'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./task-claiming-setup');

var dashboardPage = require('../pages/dashboard');


describe('Task Claiming Spec', function() {

  describe('claim and unclaim', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should claim a task', function() {

      // given
      dashboardPage.taskFilters.selectFilter(0);

      // when
      dashboardPage.taskList.selectTask(0);
      dashboardPage.currentTask.claim();

      // then
      expect(dashboardPage.currentTask.isTaskClaimed()).to.eventually.be.true;
      expect(dashboardPage.currentTask.claimedUser()).to.eventually.eql('Steve Hentschi');
    });


    it('should check the history - claim', function() {

      // when
      dashboardPage.currentTask.history.selectTab();

      // then
      expect(dashboardPage.currentTask.history.eventType(0)).to.eventually.eql('Claim');
      expect(dashboardPage.currentTask.history.subEventNewValue(0)).to.eventually.eql('admin');
      expect(dashboardPage.currentTask.history.operationUser(0)).to.eventually.eql('admin');
    });


    it('should check assignee in list of tasks - claim', function() {

      // then
      expect(dashboardPage.taskList.taskAssignee(0)).to.eventually.eql('Steve Hentschi');
    });


    it('should unclaim a task', function() {

      // given
      browser.sleep(1000);

      // when
      dashboardPage.currentTask.unclaim();

      // then
      expect(dashboardPage.currentTask.isTaskClaimed()).to.eventually.be.false;
    });


    it('should check the history - unclaim', function() {

      // when
      dashboardPage.currentTask.history.selectTab();

      // then
      expect(dashboardPage.currentTask.history.eventType(0)).to.eventually.eql('Assign');
      expect(dashboardPage.currentTask.history.subEventOriginalValue(0)).to.eventually.eql('admin');
      expect(dashboardPage.currentTask.history.operationUser(0)).to.eventually.eql('admin');
    });


    it('should check assignee in list of tasks - unclaim', function() {

      // then
      expect(dashboardPage.taskList.taskAssigneeField(0).isPresent()).to.eventually.be.false;
    });

  });


  describe('change assignee name', function() {

    before(function() {
        return testHelper(setupFile.setup2, function() {

          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should validate assignee', function() {

      // when
      dashboardPage.taskFilters.selectFilter(0);
      dashboardPage.taskList.selectTask(0);

      // then
      expect(dashboardPage.currentTask.isTaskClaimed()).to.eventually.be.true;
    });


    it('should change assignee by entering user id', function() {

      // when
      dashboardPage.currentTask.editClaimedUser('admin');

      // then
      expect(dashboardPage.currentTask.claimedUser()).to.eventually.eql('Steve Hentschi');
    });


    it('should check user id in edit mode', function() {

      // when
      dashboardPage.currentTask.clickClaimedUserField();

      // then
      expect(dashboardPage.currentTask.claimedUserFieldEditMode().getAttribute('value')).to.eventually.eql('admin');
    });


    it('should cancel editing', function() {

      // when
      dashboardPage.currentTask.cancelEditClaimedUser();

      // then
      expect(dashboardPage.currentTask.claimedUser()).to.eventually.eql('Steve Hentschi');
    });


    it('should change assigne by entering user name', function() {

      // when
      dashboardPage.currentTask.editClaimedUser('test');

      // then
      expect(dashboardPage.currentTask.claimedUser()).to.eventually.eql('Montgomery QA');
    });


    it('should check user name in edit mode', function() {

      // when
      dashboardPage.currentTask.clickClaimedUserField();

      // then
      expect(dashboardPage.currentTask.claimedUserFieldEditMode().getAttribute('value')).to.eventually.eql('test');

      // finally
      dashboardPage.currentTask.cancelEditClaimedUser();
    });


    it('does not change the assignee if it does not exists', function() {

      // when
      dashboardPage.currentTask.editClaimedUser('test');

      // then
      expect(dashboardPage.currentTask.claimedUser()).to.eventually.eql('Montgomery QA');

      // when
      dashboardPage.currentTask.editClaimedUser('testa');

      // then
      expect(dashboardPage.currentTask.claimedUserField().isDisplayed()).to.eventually.eql(true);
    });

  });

});
