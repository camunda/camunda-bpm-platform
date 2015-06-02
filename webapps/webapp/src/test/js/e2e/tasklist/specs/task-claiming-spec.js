'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./task-claiming-setup');

var dashboardPage = require('../pages/dashboard');


describe('Task Claiming Spec', function() {

  before(function() {
    return testHelper(setupFile, function() {

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
    expect(dashboardPage.currentTask.claimedUserName()).to.eventually.eql('Steve Hentschi');
  });


  it('should check the history - claim', function() {

    // when
    dashboardPage.currentTask.history.selectTab();

    // then
    expect(dashboardPage.currentTask.history.eventType(0)).to.eventually.eql('Claim');
    expect(dashboardPage.currentTask.history.subEventNewValue(0)).to.eventually.eql('admin');
    expect(dashboardPage.currentTask.history.operationUser(0)).to.eventually.eql('admin');
  });


  it('should unclaim a task', function() {

    // given
    browser.sleep(1000);

    // when
    dashboardPage.currentTask.unclaim();

    // then
    expect(dashboardPage.currentTask.claimedUserName()).to.eventually.eql('Claim');
  });


  it('should check the history - unclaim', function() {

    // when
    dashboardPage.currentTask.history.selectTab();

    // then
    expect(dashboardPage.currentTask.history.eventType(0)).to.eventually.eql('Assign');
    expect(dashboardPage.currentTask.history.subEventOriginalValue(0)).to.eventually.eql('admin');
    expect(dashboardPage.currentTask.history.operationUser(0)).to.eventually.eql('admin');
  });

});
