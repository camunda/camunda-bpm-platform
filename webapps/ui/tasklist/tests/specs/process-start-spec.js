/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false, driver: false, until: false */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./process-start-setup');

var dashboardPage = require('../pages/dashboard');
var startDialogPage = dashboardPage.startProcess;


describe('Tasklist Start Spec', function () {

  describe('start process dialog', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.navigateTo();
      });
    });

    afterEach(function() {
      startDialogPage.closeStartDialog();
    });

    it('should open', function() {

      // when
      dashboardPage.startProcess.openStartDialog();

      // then
      expect(startDialogPage.searchProcessInput().isDisplayed()).to.eventually.be.true;
    });


    it('should provide a list of processes that can be selected', function() {

      // when
      startDialogPage.openStartDialogAndSelectProcess(1);

      // then
      expect(startDialogPage.startButton().isEnabled()).to.eventually.be.true;
    });


    it('should allow to search for process definition', function() {

      // given
      dashboardPage.startProcess.openStartDialog();
      expect(startDialogPage.processList().count()).to.eventually.eql(2);

      // when
      startDialogPage.searchProcessInput('User');

      // then
      expect(startDialogPage.processList().count()).to.eventually.eql(1);
      expect(startDialogPage.processList().first().getText()).to.eventually.eql('User Tasks');
    });


    it('should search case sensitivity', function() {

      // given
      dashboardPage.startProcess.openStartDialog();
      expect(startDialogPage.processList().count()).to.eventually.eql(2);

      // when
      startDialogPage.searchProcessInput('user');

      // then
      expect(startDialogPage.processList().count()).to.eventually.eql(0);
    });


    it('should allow to go back on dialog page one', function() {

      // when
      startDialogPage.openStartDialogAndSelectProcess(1);
      startDialogPage.backButton().click()

      // then
      expect(startDialogPage.searchProcessInput().isDisplayed()).to.eventually.be.true;
    });

  });


  describe('generic start form', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.navigateTo();
      });
    });

    beforeEach(function() {
      startDialogPage.openStartDialogAndSelectProcess(1);
    });

    afterEach(function() {
      startDialogPage.closeStartDialog();
    });

    it('should disable start button when adding variable', function() {

      // given
      expect(startDialogPage.genericStartForm.addVariableButton().isDisplayed()).is.eventually.be.true;

      // when
      startDialogPage.genericStartForm.addVariableButton().click()

      // then
      expect(startDialogPage.startButton().isEnabled()).to.eventually.be.false;
    });


    it('should enable start button after adding variable', function() {

      // given
      expect(startDialogPage.genericStartForm.addVariableButton().isDisplayed()).is.eventually.be.true;

      // when
      startDialogPage.genericStartForm.addVariableButton().click();
      startDialogPage.genericStartForm.variableNameInput(0, 'hans');
      startDialogPage.genericStartForm.variableTypeInput(0, 'String');
      startDialogPage.genericStartForm.variableValueInput(0, 'asdf');

      // then
      expect(startDialogPage.startButton().isEnabled()).to.eventually.be.true;
    });


    it('should allow to add multiple variables', function() {

      // given
      expect(startDialogPage.genericStartForm.variableList().count()).to.eventually.eql(0);

      // when
      startDialogPage.genericStartForm.addVariable('var_1', 'Date', '2012-12-12T12:12:12');
      startDialogPage.genericStartForm.addVariable('var_2', 'Short', '123456');

      // then
      expect(startDialogPage.genericStartForm.variableList().count()).to.eventually.eql(2);
    });


    it('should allow to remove a variable', function() {

      // given
      startDialogPage.genericStartForm.addVariable('var_1', 'Long', '123456789');
      expect(startDialogPage.genericStartForm.variableList().count()).to.eventually.eql(1);

      // when
      startDialogPage.genericStartForm.removeVariable(0);

      // then
      expect(startDialogPage.genericStartForm.variableList().count()).to.eventually.eql(0);
      expect(startDialogPage.startButton().isEnabled()).to.eventually.be.true;
    });


    it('should validate unique variable names', function() {

      // when
      startDialogPage.genericStartForm.addVariableButton().click();
      startDialogPage.genericStartForm.variableNameInput(0, 'hans');

      startDialogPage.genericStartForm.addVariableButton().click();
      startDialogPage.genericStartForm.variableNameInput(1, 'hans');

      // then
      expect(startDialogPage.genericStartForm.variableNameHelpText(0)).is.eventually.eql('Name must be unique');
      expect(startDialogPage.genericStartForm.variableNameHelpText(1)).is.eventually.eql('Name must be unique');
      expect(startDialogPage.genericStartForm.isVariableNameInputValide(0)).is.eventually.be.false;
      expect(startDialogPage.genericStartForm.isVariableNameInputValide(1)).is.eventually.be.false;
    });


    it('should validate boolean variable type/value', function() {

      // given
      startDialogPage.genericStartForm.addVariableButton().click();
      startDialogPage.genericStartForm.variableNameInput(0, 'hans');

      // when
      startDialogPage.genericStartForm.variableTypeInput(0, 'Boolean');
      expect(startDialogPage.genericStartForm.variableValueInput(0).isSelected()).to.eventually.be.false;

      startDialogPage.genericStartForm.variableValueInput(0).click();

      // then
      expect(startDialogPage.genericStartForm.variableValueInput(0).isSelected()).to.eventually.be.true;
    });


    it('should validate double variable type/value', function() {

      // given
      startDialogPage.genericStartForm.addVariableButton().click();
      startDialogPage.genericStartForm.variableNameInput(0, 'hans');

      // when
      startDialogPage.genericStartForm.variableTypeInput(0, 'Double');

      // invalid input
      startDialogPage.genericStartForm.variableValueInput(0, 'abcde');
      expect(startDialogPage.genericStartForm.variableValueHelpText(0)).to.eventually.eql('Only a Double value is allowed');
      expect(startDialogPage.genericStartForm.isVariableValueInputValide(0)).is.eventually.be.false;

      // valid input
      startDialogPage.genericStartForm.variableValueInput(0).clear();
      startDialogPage.genericStartForm.variableValueInput(0, '12.99');
      expect(startDialogPage.genericStartForm.variableValueInput(0).isEnabled()).to.eventually.be.true;
    });


    it('should allow to add business key', function() {

      // given
      expect(startDialogPage.genericStartForm.businessKeyInput().isDisplayed()).is.eventually.be.true;

      // when
      startDialogPage.genericStartForm.businessKeyInput('MyBusinessKey');

      // then
      expect(startDialogPage.startButton().isEnabled()).to.eventually.be.true;
    });

  });


  describe('start process', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.navigateTo();
        dashboardPage.startProcess.openStartDialogAndSelectProcess('User Tasks');
      });
    });

    it('should enter variables and business key', function() {

      // when
      startDialogPage.genericStartForm.addVariable('var_1', 'Date', '2012-12-12T12:12:12');
      startDialogPage.genericStartForm.addVariable('var_2', 'Short', '123');
      startDialogPage.genericStartForm.addVariable('var_3', 'String', 'blaw blaw');
      startDialogPage.genericStartForm.addVariable('var_4', 'Integer', '123456789');
      startDialogPage.genericStartForm.addVariable('var_5', 'Double', '1.0099');
      startDialogPage.genericStartForm.addVariable('var_6', 'Long', '12345678912345678');
      startDialogPage.genericStartForm.addVariable('var_7', 'Boolean', 'true');

      startDialogPage.genericStartForm.businessKeyInput('myBusinessKey01');

      // then
      expect(startDialogPage.genericStartForm.variableList().count()).to.eventually.eql(7);
    });


    it('should close the dialog', function() {

      // when
      startDialogPage.startProcess();
      browser.sleep(300);
      // then

      // following line probably causing stale element reference error in Jenkins
      // expect(startDialogPage.startProcessDialog().isPresent()).to.eventually.be.false;

      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
    });


    it('should validate variables in the list of tasks', function() {

      // when
      dashboardPage.taskList.showMoreVariables(0);

      // then
      expect(dashboardPage.taskList.taskVariableLabel(0,0).getText()).to.eventually.eql('var_1:');
      expect(dashboardPage.taskList.taskVariableValue(0,0).getText()).to.eventually.eql('12 December 2012 12:12');
      expect(dashboardPage.taskList.taskVariableLabel(0,1).getText()).to.eventually.eql('var_2:');
      expect(dashboardPage.taskList.taskVariableValue(0,1).getText()).to.eventually.eql('123');
      expect(dashboardPage.taskList.taskVariableLabel(0,2).getText()).to.eventually.eql('var_3:');
      expect(dashboardPage.taskList.taskVariableValue(0,2).getText()).to.eventually.eql('blaw blaw');
      expect(dashboardPage.taskList.taskVariableLabel(0,3).getText()).to.eventually.eql('var_4:');
      expect(dashboardPage.taskList.taskVariableValue(0,3).getText()).to.eventually.eql('123456789');
      expect(dashboardPage.taskList.taskVariableLabel(0,4).getText()).to.eventually.eql('var_5:');
      expect(dashboardPage.taskList.taskVariableValue(0,4).getText()).to.eventually.eql('1.0099');
      expect(dashboardPage.taskList.taskVariableLabel(0,5).getText()).to.eventually.eql('var_6:');
      expect(dashboardPage.taskList.taskVariableValue(0,5).getText()).to.eventually.eql('12345678912345678');
      expect(dashboardPage.taskList.taskVariableLabel(0,6).getText()).to.eventually.eql('var_7:');
      expect(dashboardPage.taskList.taskVariableValue(0,6).getText()).to.eventually.eql('true');
    });

  });

  describe('multi tenancy', function(){

    // TODO avoid multiple setups
    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should display the tenand id of process definitions', function() {

      // when
      dashboardPage.startProcess.openStartDialog();

      // then
      expect(startDialogPage.processList().count()).to.eventually.eql(2);

      expect(startDialogPage.processTenantIdField(0).isPresent()).to.eventually.be.false;
      expect(startDialogPage.processTenantIdField(1).isPresent()).to.eventually.be.true;
      expect(startDialogPage.processTenantIdField(1).getText()).to.eventually.eql('tenant1');
    });

    it('should start an instance of a process definition with tenant id', function() {

      // when
      dashboardPage.startProcess.selectProcessByIndex(1);
      startDialogPage.startProcess();

      // then
      expect(startDialogPage.startProcessDialog().isPresent()).to.eventually.be.false;
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);

      dashboardPage.taskList.selectTask(0);
      dashboardPage.waitForElementToBeVisible(dashboardPage.currentTask.taskName());

      // then
      expect(dashboardPage.currentTask.taskTenantIdField().isPresent()).to.eventually.be.true;
      expect(dashboardPage.currentTask.taskTenantIdField().getText()).to.eventually.eql('tenant1');
    });

    it('should start an instance of a process definition which belong to no tenant', function() {

      // when
      dashboardPage.startProcess.openStartDialog();
      dashboardPage.startProcess.selectProcessByIndex(0);
      startDialogPage.startProcess();

      // then
      expect(startDialogPage.startProcessDialog().isPresent()).to.eventually.be.false;
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);

      dashboardPage.taskList.selectTask(0);
      dashboardPage.waitForElementToBeVisible(dashboardPage.currentTask.taskName());

      // then
      expect(dashboardPage.currentTask.taskTenantIdField().isPresent()).to.eventually.be.false;
    });

  });

});
