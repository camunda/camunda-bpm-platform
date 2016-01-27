/* jshint ignore:start */
'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/process-definition');
var instancePage = require('../pages/process-instance');


describe('Cockpit Process Instance Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });

    it('should go to process instance view', function() {

      // given
      definitionPage.processInstancesTab.instanceId(1).then(function(instanceId) {

        // when
        definitionPage.processInstancesTab.selectInstanceId(1);

        // then
        expect(instancePage.pageHeaderProcessInstanceName()).to.eventually.eql(instanceId);
      });
    });


    it('should go to User Tasks tab', function() {

      // when
      instancePage.userTasksTab.selectTab();

      // then
      expect(instancePage.userTasksTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.userTasksTab.tabName()).to.eventually.eql(instancePage.userTasksTab.tabLabel);
    });


    it('should go to Called Process Instances tab', function() {

      // when
      instancePage.calledInstancesTab.selectTab();

      // then
      expect(instancePage.calledInstancesTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.calledInstancesTab.tabName()).to.eventually.eql(instancePage.calledInstancesTab.tabLabel);
    });


    it('should go to Incidents tab', function() {

      // when
      instancePage.incidentsTab.selectTab();

      // then
      expect(instancePage.incidentsTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.incidentsTab.tabName()).to.eventually.eql(instancePage.incidentsTab.tabLabel);
    });


    it('should go to Variables tab', function() {

      // when
      instancePage.variablesTab.selectTab();

      // then
      expect(instancePage.variablesTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.variablesTab.tabName()).to.eventually.eql(instancePage.variablesTab.tabLabel);
    });

  });


  describe('edit User Task assignee', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstanceId(0);
      });
    });

    it('should open user tasks tab', function() {

      // when
      instancePage.userTasksTab.selectTab();

      // then
      expect(instancePage.userTasksTab.table().count()).to.eventually.eql(1);
      expect(instancePage.userTasksTab.activity(0).getText()).to.eventually.eql('User Task 1');
    });


    it('should select user task', function() {

      // when
      instancePage.userTasksTab.activity(0).click();

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
    });


    it('should add new assignee', function() {

      // when
      instancePage.userTasksTab.addNewAssignee(0, 'Franz');

      // then
      expect(instancePage.userTasksTab.assignee(0).getText()).is.eventually.eql('Franz');
    });

  });

  describe('edit User Task identity links', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstanceId(0);
        instancePage.userTasksTab.selectTab();
      });
    });

    describe('edit group identity links', function() {

      before(function() {
        instancePage.userTasksTab.clickChangeGroupIdentityLinksButton();
      });

      it('opens', function() {
        expect(instancePage.userTasksTab.modal.dialog().isDisplayed()).to.eventually.eql(true);
      });


      it('has a title', function() {
        expect(instancePage.userTasksTab.modal.title()).to.eventually.eql('Manage groups');
      });


      it('initially contains two groups', function () {
        expect(instancePage.userTasksTab.modal.elements().count()).to.eventually.eql(2);
      });


      it('adds a new group identity link', function() {
        // when
        instancePage.userTasksTab.modal.nameInput().clear().sendKeys('my-super-group');
        instancePage.userTasksTab.modal.clickAddNameButton();

        // then
        expect(instancePage.userTasksTab.modal.elements().count()).to.eventually.eql(3);
      });

      it('deletes a group identity link', function() {
        // when
        instancePage.userTasksTab.modal.clickDeleteNameButton('my-super-group');

        // then
        expect(instancePage.userTasksTab.modal.elements().count()).to.eventually.eql(2);
      });


      it('closes the dialog', function() {
        // when
        instancePage.userTasksTab.modal.clickCloseButton();

        // then
        expect(instancePage.userTasksTab.modal.dialog().isPresent()).to.eventually.eql(false);
      });

    });


    describe('edit user identity links', function() {

      before(function() {
        instancePage.userTasksTab.clickChangeUserIdentityLinksButton();
      });

      it('opens', function() {
        expect(instancePage.userTasksTab.modal.dialog().isDisplayed()).to.eventually.eql(true);
      });


      it('has a title', function() {
        expect(instancePage.userTasksTab.modal.title()).to.eventually.eql('Manage users');
      });


      it('initially contains no users', function () {
        expect(instancePage.userTasksTab.modal.elements().count()).to.eventually.eql(0);
      });


      it('adds a new user identity link', function() {
        // when
        instancePage.userTasksTab.modal.nameInput().clear().sendKeys('superman');
        instancePage.userTasksTab.modal.clickAddNameButton();

        // then
        expect(instancePage.userTasksTab.modal.elements().count()).to.eventually.eql(1);
      });

      it('deletes a group identity link', function() {
        // when
        instancePage.userTasksTab.modal.clickDeleteNameButton('superman');

        // then
        expect(instancePage.userTasksTab.modal.elements().count()).to.eventually.eql(0);
      });


      it('closes the dialog', function() {
        // when
        instancePage.userTasksTab.modal.clickCloseButton();

        // then
        expect(instancePage.userTasksTab.modal.dialog().isPresent()).to.eventually.eql(false);
      });

    });

  });

  describe('diagram interaction', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstanceId(0);
      });
    });

    it('should display process diagram', function() {

      // then
      expect(instancePage.diagram.diagramElement().isDisplayed()).to.eventually.be.true;
    });


    it('should select unselectable task', function() {

      // when
      instancePage.diagram.selectActivity('UserTask_2');

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_2')).to.eventually.be.false;
    });


    it('should display the number of concurrent activities', function() {

      // then
      expect(instancePage.diagram.instancesBadgeFor('UserTask_1').getText()).to.eventually.eql('1');
    });


    it('should process clicks in diagram', function() {

      // given
      instancePage.instanceTree.selectInstance('User Task 1');
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;

      // when
      instancePage.diagram.deselectAll();

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
      expect(instancePage.instanceTree.isInstanceSelected('User Task 1')).to.eventually.be.false;
    });


    it('should keep selection after page refresh', function() {

      // given
      instancePage.instanceTree.selectInstance('User Task 1');
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;

      // when
      browser.getCurrentUrl().then(function(url) {
        browser.get(url).then(function() {
          browser.sleep(500);
        });
      });

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
    });


    it('should reflect the tree view selection in diagram', function() {

      // given
      instancePage.instanceTree.selectInstance('User Task 1');
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;

      // when
      instancePage.instanceTree.deselectInstance('User Task 1');

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
    });

  });


  describe('cancel instance', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });

    it('cancel instances', function() {

      // given
      definitionPage.processInstancesTab.table().count().then(function(numberOfInstances) {
        definitionPage.processInstancesTab.selectInstanceId(0);

        // when
        instancePage.cancelInstance.cancelInstance();

        // then
        expect(definitionPage.processInstancesTab.table().count()).to.eventually.eql(numberOfInstances-1);
      });
    });

  });

  describe('Multi Instance Incidents', function() {

    before(function() {
      return testHelper(setupFile.setup2, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });

    it('show incidents badge', function() {

      // given
      definitionPage.processInstancesTab.table().count().then(function(numberOfInstances) {
        definitionPage.processInstancesTab.selectInstanceId(0);
        browser.sleep(1000);
        // then
        expect(instancePage.diagram.incidentsBadgeFor('ServiceTask_1').getText()).to.eventually.eql('!');
      });
    });

  });


  describe('Bulk job retry', function () {
    before(function () {
      return testHelper(setupFile.setup3, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    function goToInstanceJobRetryModal(name) {
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.deployedProcessesList.selectProcessByName(name);
      element(by.css('.ctn-content-bottom .instance-id a')).click();
      element(by.css('.ctn-toolbar [tooltip~="Retries"]')).click();
    }

    function getCheckedCheckboxes() {
      return element.all(by.css('.modal-body [type=checkbox]:checked'));
    }


    describe('when only 1 job failed', function () {
      before(function () {
        goToInstanceJobRetryModal('mi-incident');
      });

      it('pre-selects the job', function () {
        expect(element(by.css('.modal-body')).isDisplayed()).to.eventually.eql(true);
        expect(getCheckedCheckboxes().count()).to.eventually.eql(2);
      });
    });



    describe('when 4 jobs failed', function () {
      before(function () {
        goToInstanceJobRetryModal('4 Failing Service Tasks');
      });

      it('pre-selects the jobs', function () {
        expect(element(by.css('.modal-body')).isDisplayed()).to.eventually.eql(true);
        expect(getCheckedCheckboxes().count()).to.eventually.eql(5);
      });
    });



    describe('when 7 job failed', function () {
      before(function () {
        goToInstanceJobRetryModal('7 Failing Service Tasks');
      });

      it('does not pre-select the jobs', function () {
        expect(element(by.css('.modal-body')).isDisplayed()).to.eventually.eql(true);
        expect(getCheckedCheckboxes().count()).to.eventually.eql(0);
      });
    });
  });
});
