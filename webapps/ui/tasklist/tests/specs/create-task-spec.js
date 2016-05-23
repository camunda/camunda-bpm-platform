/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false, driver: false, until: false */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setups = require('./create-task-setup');

var dashboardPage = require('../pages/dashboard');
var createTaskDialogPage = dashboardPage.createTask;


describe('Tasklist Create Task Spec', function () {

  describe('create task without tenant', function() {

    before(function() {
      return testHelper(function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.navigateTo();
      });
    });

    it('should open', function() {

      // when
      dashboardPage.createTask.openCreateDialog();

      // then
      expect(createTaskDialogPage.taskNameField().isDisplayed()).to.eventually.be.true;
      expect(createTaskDialogPage.taskAssigneeField().isDisplayed()).to.eventually.be.true;
      expect(createTaskDialogPage.taskTenantIdField().isPresent()).to.eventually.be.false;
    });

    it('should close the dialog', function() {

      // when
      createTaskDialogPage.closeCreateDialog();
      browser.sleep(300);

      // then
      expect(createTaskDialogPage.createTaskDialog().isPresent()).to.eventually.be.false;
    });

  });


  describe('create task with two tenants', function () {

    before(function() {
      return testHelper(setups.setup2 ,function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.navigateTo();
      });
    });

    it('should open', function() {

        // when
        dashboardPage.createTask.openCreateDialog();

        // then
        expect(createTaskDialogPage.taskNameField().isDisplayed()).to.eventually.be.true;
        expect(createTaskDialogPage.taskAssigneeField().isDisplayed()).to.eventually.be.true;
        expect(createTaskDialogPage.taskTenantIdField().isDisplayed()).to.eventually.be.true;
    });

    it('should save new task', function() {

      // when
      createTaskDialogPage.taskNameInput('foo');
      createTaskDialogPage.taskAssigneeInput('admin');

      createTaskDialogPage.saveTask();

      expect(dashboardPage.taskFilters.isFilterSelected(0)).to.eventually.be.true;

      // then
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
    });


    it('should select created task', function() {

      // when
      dashboardPage.taskList.selectTask('foo');

      // then
      expect(dashboardPage.currentTask.taskTenantIdField().isDisplayed()).to.eventually.be.true;
      expect(dashboardPage.currentTask.taskTenantIdField().getText()).to.eventually.eql('tenantOne');
    });

  });


  describe('create task with one tenant', function () {

    before(function() {
      return testHelper(setups.setup1 ,function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.navigateTo();
      });
    });

    it('should open', function() {

        // when
        dashboardPage.createTask.openCreateDialog();

        // then
        expect(createTaskDialogPage.taskNameField().isDisplayed()).to.eventually.be.true;
        expect(createTaskDialogPage.taskAssigneeField().isDisplayed()).to.eventually.be.true;
        expect(createTaskDialogPage.taskTenantIdField().isPresent()).to.eventually.be.false;
    });

    it('should save new task', function() {

      // when
      createTaskDialogPage.taskNameInput('foo');
      createTaskDialogPage.taskAssigneeInput('admin');

      createTaskDialogPage.saveTask();

      expect(dashboardPage.taskFilters.isFilterSelected(0)).to.eventually.be.true;

      // then
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
    });


    it('should select created task', function() {

      // when
      dashboardPage.taskList.selectTask('foo');

      // then
      expect(dashboardPage.currentTask.taskTenantIdField().isDisplayed()).to.eventually.be.true;
      expect(dashboardPage.currentTask.taskTenantIdField().getText()).to.eventually.eql('tenantOne');
    });

  });

});
