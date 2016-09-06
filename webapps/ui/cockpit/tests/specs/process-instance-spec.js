/* jshint ignore:start */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./process-setup');
var CamSDK = require('camunda-commons-ui/vendor/camunda-bpm-sdk');

var dashboardPage = require('../pages/dashboard');
var processesPage = require('../pages/processes');
var definitionPage = require('../pages/process-definition');
var instancePage = require('../pages/process-instance');


describe('Cockpit Process Instance Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
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
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
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
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
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
        var el = element(by.css('[ng-click="close($event)"]'));
        el.isPresent().then(function (yepNope) {
          if (!yepNope) { return; }
          el.isDisplayed().then(function (yepNope) {
            if (!yepNope) { return; }
            el.click();
          });
        });
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
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
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
      instancePage.sidebarTabClick('Filter');
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
      instancePage.sidebarTabClick('Filter');
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
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
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
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
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

  // CAM-5846
  describe('Retry external task incident', function() {
    before(function(done) {
      return testHelper(setupFile.setup4, function() {
        var camClient = new CamSDK.Client({
          mock: false,
          apiUri: 'http://localhost:8080/engine-rest'
        });

        var ExternalTask = camClient.resource('external-task');
        ExternalTask.fetchAndLock({
          workerId: 'myWorker',
          maxTasks: 1,
          topics: [{
            topicName: 'must-have-topic',
            lockDuration: 10000,
            variables: []
          }]
        }, function(err, res) {
          if(err) {
            return done(err);
          }

          if (res.length > 0) {
            var extTask = res[0].id;

            ExternalTask.failure({
              id: extTask,
              workerId: 'myWorker',
              errorMessage : 'must-have-error-message.',
              retries : 0,
              retriesTimeout : 10
            }, function(err) {
              if(err) {
                return done(err);
              }

              done();
            });
          }
        });

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcessByName("Failed external task");
        element(by.css('.ctn-content-bottom .instance-id [ng-transclude] a')).click();
        instancePage.incidentsTab.selectTab();
      });
    });

    it('should have a retry button', function() {
      var retriesButton = instancePage.incidentsTab.incidentRetryAction(0);
      expect(retriesButton.isDisplayed()).to.eventually.eql(true);
    });
  });


  describe('Bulk job retry', function () {
    before(function () {
      return testHelper(setupFile.setup3, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
      });
    });

    function goToInstanceJobRetryModal(name) {
      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.goToSection('Processes');
      processesPage.deployedProcessesList.selectProcessByName(name);
      element(by.css('.ctn-content-bottom .instance-id [ng-transclude] a')).click();
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

  describe('multi tenancy', function() {

    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
      });
    });

    it('should display tenant id of instance', function() {
      dashboardPage.goToSection('Processes');

      // through sorting, first process definition is without tenant, second has tenant
      processesPage.deployedProcessesList.selectProcess(1);
      definitionPage.processInstancesTab.selectInstanceId(0);

      // when
      instancePage.sidebarTabClick('Information');

      // then
      expect(instancePage.information.tenantId()).to.eventually.contain('tenant1');
    });

    it('should not display tenant id of instance if not exists', function() {

      dashboardPage.navigateToWebapp('Cockpit');
      dashboardPage.goToSection('Processes');
      // through sorting, first process definition is without tenant, second has tenant
      processesPage.deployedProcessesList.selectProcess(0);
      definitionPage.processInstancesTab.selectInstanceId(0);

      // when
      instancePage.sidebarTabClick('Information');

      // then
      expect(instancePage.information.tenantId()).to.eventually.contain('null');
    });

  });

  describe('search widget', function() {
    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');

        processesPage.deployedProcessesList.selectProcess(1);
        definitionPage.processInstancesTab.selectInstanceId(0);

        instancePage.variablesTab.selectTab();
      });
    });

    afterEach(function() {
      instancePage.search.clearSearch();
    });

    it('should display search widget', function() {
      expect(instancePage.search.formElement().isDisplayed()).to.eventually.be.true;
    });

    it('should have Variable Name filter with = operator', function() {
      //when
      instancePage.search.createSearch('Variable Name', '=', 'test');

      //then
      var eqOperator = instancePage
        .search
        .formElement()
        .element(by.cssContainingText('[tooltip="Operator"]', '='));

      expect(eqOperator.isDisplayed()).to.eventually.be.true;
    });

    it('should have Variable Name filter with like operator', function() {
      //when
      instancePage.search.createSearch('Variable Name', 'like', 'test');

      //then
      var likeOperator = instancePage
        .search
        .formElement()
        .element(by.cssContainingText('[tooltip="Operator"]', 'like'));

      expect(likeOperator.isDisplayed()).to.eventually.be.true;
    });

    it('should display only one variable with name test', function() {
      //when
      instancePage.search.createSearch('Variable Name', '=', 'test');

      //then
      expect(instancePage.variablesTab.table().count()).to.eventually.eql(1);
    });
  });
});
