/* jshint ignore:start */
'use strict';

var fs = require('fs');

var testHelper = require('../../test-helper');
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/process-definition');
var instancePage = require('../pages/process-instance');

describe('Cockpit Process Instance Spec', function() {

  describe('page navigation', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });


    it('should go to process instance view', function() {

      // given
      definitionPage.processInstancesTab.instanceId(1).then(function(instanceIdy) {

        // when
        definitionPage.processInstancesTab.selectInstance(1);

        // then
        expect(instancePage.pageHeaderProcessInstanceName()).to.eventually.eql(instanceIdy);
      });
    });


    it('should go to User Tasks tab', function() {

      // when
      instancePage.userTasksTab.selectTab();

      // then
      expect(instancePage.userTasksTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.userTasksTab.tabName()).to.eventually.eql(instancePage.userTasksTab.tabLabel)
    });


    it('should go to Called Process Instances tab', function() {

      // when
      instancePage.calledInstancesTab.selectTab();

      // then
      expect(instancePage.calledInstancesTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.calledInstancesTab.tabName()).to.eventually.eql(instancePage.calledInstancesTab.tabLabel)
    });


    it('should go to Incidents tab', function() {

      // when
      instancePage.incidentsTab.selectTab();

      // then
      expect(instancePage.incidentsTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.incidentsTab.tabName()).to.eventually.eql(instancePage.incidentsTab.tabLabel)
    });


    it('should go to Variables tab', function() {

      // when
      instancePage.variablesTab.selectTab();

      // then
      expect(instancePage.variablesTab.isTabSelected()).to.eventually.be.true;
      expect(instancePage.variablesTab.tabName()).to.eventually.eql(instancePage.variablesTab.tabLabel)
    });

  });


  describe('edit User Task assignee', function() {

    before(function() {
      return testHelper(setupFile, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstance(0);
      });
    });


    it('should open user tasks tab', function() {

      // when
      instancePage.userTasksTab.selectTab();

      // then
      expect(instancePage.userTasksTab.table().count()).to.eventually.eql(1);
      expect(instancePage.userTasksTab.userTaskName(0)).to.eventually.eql('User Task 1');
    });


    it('should select user task', function() {

      // when
      instancePage.userTasksTab.selectUserTask(0);

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
    });

  });


  describe('work with variables', function() {

    before(function() {
      return testHelper(setupFile, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstance(0);
      });
    });


    it('should add String variable', function() {

      // given
      expect(instancePage.variablesTab.table().count()).to.eventually.eql(3);

      // when
      instancePage.addVariable.addVariable('myTestVar', 'String', '12345');

      // then
      expect(instancePage.variablesTab.table().count()).to.eventually.eql(4);

      instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'myTestVar')
        .then(function(idx) {
          expect(instancePage.variablesTab.variableName(idx)).to.eventually.eql('myTestVar');
          expect(instancePage.variablesTab.variableType(idx)).to.eventually.eql('String');
          expect(instancePage.variablesTab.variableValue(idx)).to.eventually.eql('12345');
          expect(instancePage.variablesTab.variableScopeName(idx)).to.eventually.eql('User Tasks');
        });
    });


    it('should add Boolean variable', function() {

      // given
      instancePage.variablesTab.table().count().then(function(varCountBefore) {

        // when
        instancePage.addVariable.addVariable('myBooleanVar', 'Boolean', true);

        // then
        expect(instancePage.variablesTab.table().count()).to.eventually.eql(varCountBefore+1);

        instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'myBooleanVar')
          .then(function(idx) {
            expect(instancePage.variablesTab.variableName(idx)).to.eventually.eql('myBooleanVar');
            expect(instancePage.variablesTab.variableType(idx)).to.eventually.eql('Boolean');
            expect(instancePage.variablesTab.variableValue(idx)).to.eventually.eql('true');
            expect(instancePage.variablesTab.variableScopeName(idx)).to.eventually.eql('User Tasks');
          });
      });
    });


    it('should add a NULL process variable', function() {

      //given
      instancePage.variablesTab.table().count().then(function(varCountBefore) {

        // when
        instancePage.addVariable.addVariable('myNullVar', 'Null');

        // then
        expect(instancePage.variablesTab.table().count()).to.eventually.eql(varCountBefore+1);
        instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'myNullVar')
        .then(function(idx) {
          expect(instancePage.variablesTab.variableName(idx)).to.eventually.eql('myNullVar');
          expect(instancePage.variablesTab.variableType(idx)).to.eventually.eql('Null');
          expect(instancePage.variablesTab.variableValue(idx)).to.eventually.eql('');
          expect(instancePage.variablesTab.variableScopeName(idx)).to.eventually.eql('User Tasks');
        });
      });
    });


    it('should add an Object process variable', function() {

      // given
      instancePage.variablesTab.table().count().then(function(varCountBefore) {

        // when
        instancePage.addVariable.addVariable('myObjectVar', 'Object', {
          value: '',
          objectTypeName: 'java.lang.Object',
          serializationDataFormat: 'application/x-java-serialized-object'
        });

        // then
        expect(instancePage.variablesTab.table().count()).to.eventually.eql(varCountBefore+1);
        instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'myObjectVar')
        .then(function(idx) {
          expect(instancePage.variablesTab.variableName(idx)).to.eventually.eql('myObjectVar');
          expect(instancePage.variablesTab.variableType(idx)).to.eventually.eql('Object');
          expect(instancePage.variablesTab.variableValue(idx)).to.eventually.eql('java.lang.Object');
          expect(instancePage.variablesTab.variableScopeName(idx)).to.eventually.eql('User Tasks');
        });
      });
    });


    it('should change variable', function() {

      // given
      instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'test')
        .then(function(idx) {
          expect(instancePage.variablesTab.variableType(idx)).to.eventually.eql('Double');
          expect(instancePage.variablesTab.variableValue(idx)).to.eventually.eql('1.49');

          // when
          instancePage.variablesTab.editVariableButton(idx).click().then(function() {
            instancePage.variablesTab.editVariableValue().clear();
            instancePage.variablesTab.editVariableValue('1.5');
            instancePage.variablesTab.editVariableType('String');
            instancePage.variablesTab.editVariableConfirmButton().click();
          });

          // then
          expect(instancePage.variablesTab.variableName(idx)).to.eventually.eql('test');
          expect(instancePage.variablesTab.variableValue(idx)).to.eventually.eql('1.5');
          expect(instancePage.variablesTab.variableType(idx)).to.eventually.eql('String');
        });
    });


    it('should select wrong variable type', function() {

      // given
      instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'myDate')
        .then(function(idx) {
          expect(instancePage.variablesTab.variableType(idx)).to.eventually.eql('Date');
          expect(instancePage.variablesTab.variableValue(idx)).to.eventually.eql('2011-11-11T11:11:11');

          // when
          instancePage.variablesTab.editVariableButton(idx).click().then(function() {
            instancePage.variablesTab.editVariableType('Short');
          });

          // then
          expect(instancePage.variablesTab.editVariableErrorText()).to.eventually.eql('Invalid value: Only a Short value is allowed.');
          expect(instancePage.variablesTab.editVariableConfirmButton().isEnabled()).to.eventually.be.false;

          // finaly
          instancePage.variablesTab.editVariableCancelButton().click().then(function() {
            expect(instancePage.variablesTab.inlineEditRow().isPresent()).to.eventually.be.false;
          });
        });
    });


    describe('validate add variables modal view', function() {

      before(function() {
        return testHelper(setupFile, function() {
          dashboardPage.navigateToWebapp('Cockpit');
          dashboardPage.authentication.userLogin('admin', 'admin');
          dashboardPage.deployedProcessesList.selectProcess(0);
          definitionPage.processInstancesTab.selectInstance(0);
        });
      });


      it('should open modal view', function() {

        // when
        instancePage.addVariable.addVariableButton().click()

        // then
        expect(instancePage.addVariable.modalHeading().getText()).to.eventually.eql('Add Variable to Process Instance');
      });


      it('should enter variable data', function() {

        // given
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;
        expect(instancePage.addVariable.variableTypeDropdownSelectedItem().getText()).to.eventually.eql('String');

        // when
        instancePage.addVariable.variableNameInput('myTestVariable');

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;

        // when
        instancePage.addVariable.variableValueInput('abc123$%&');

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.true;

        // when
        instancePage.addVariable.variableNameInput().clear();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;

        // finaly
        instancePage.addVariable.variableNameInput('myTestVariable');
      });


      it('should change from string to integer', function() {

        // given
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.true;

        // when
        instancePage.addVariable.variableTypeDropdown('Integer').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;
        expect(instancePage.addVariable.variableValueInfoLabel().getText()).to.eventually.eql('Only a Integer value is allowed.');
      });


      it('should change to short', function() {

        // when
        instancePage.addVariable.variableTypeDropdown('Short').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;
        expect(instancePage.addVariable.variableValueInfoLabel().getText()).to.eventually.eql('Only a Short value is allowed.');
      });


      it('should change to double', function() {

        // when
        instancePage.addVariable.variableTypeDropdown('Double').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;
        expect(instancePage.addVariable.variableValueInfoLabel().getText()).to.eventually.eql('Only a Double value is allowed.');
      });


      it('should change long', function() {

        // when
        instancePage.addVariable.variableTypeDropdown('Long').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;
        expect(instancePage.addVariable.variableValueInfoLabel().getText()).to.eventually.eql('Only a Long value is allowed.');
      });


      it('should enter valid variable value', function() {

        // when
        instancePage.addVariable.variableValueInput().clear();
        instancePage.addVariable.variableValueInput('123456789');

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.true;
        expect(instancePage.addVariable.variableValueInfoLabel().isPresent()).to.eventually.be.false;
      });


      it('should change to date', function() {

        // when
        instancePage.addVariable.variableTypeDropdown('Date').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;
        expect(instancePage.addVariable.variableValueInfoLabel().getText()).to.eventually.eql('Supported pattern \'yyyy-MM-ddTHH:mm:ss\'.');
      });


      it('should change to object', function() {

        // when
        instancePage.addVariable.variableTypeDropdown('Object').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.false;
        expect(instancePage.addVariable.objectValueInput().getAttribute('value')).to.eventually.eql('123456789');
      });


      it('should change to null', function() {

        // when
        instancePage.addVariable.variableTypeDropdown('Null').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.true;
        expect(instancePage.addVariable.variableValueInput().isPresent()).to.eventually.be.false;
      });


      it('should change to boolean', function() {

        // when
        instancePage.addVariable.variableTypeDropdown('Boolean').click();

        // then
        expect(instancePage.addVariable.addButton().isEnabled()).to.eventually.be.true;
      });

    });

  });


  describe('diagram interaction', function() {

    before(function() {
      return testHelper(setupFile, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstance(0);
      });
    });


    it('should display process diagram', function() {
      expect(instancePage.diagram.diagramElement().isDisplayed()).to.eventually.be.true;
    });


    it('should select unselectable task', function() {

      // when
      instancePage.diagram.selectActivity('UserTask_2');

      // then
      expect(instancePage.diagram.isActivitySelected('UserTask_2')).to.eventually.be.false;
    });


    it('should display the number of concurrent activities', function() {
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

});
