'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./filter-variables-setup');

var dashboardPage = require('../pages/dashboard');
var editModalPage = dashboardPage.taskFilters.editFilterPage;


describe('Tasklist Filter Variables Spec', function() {

  before(function() {
    return testHelper(setupFile.setup1, function() {

      dashboardPage.navigateToWebapp('Tasklist');
      dashboardPage.authentication.userLogin('admin', 'admin');
    });
  });

  describe('the filter variable page', function() {

    beforeEach(function() {
      dashboardPage.taskFilters.selectFilter(0);
      dashboardPage.taskFilters.editFilter(0);
      editModalPage.selectPanelByKey('variable');
    });

    afterEach(function() {
      editModalPage.closeFilter();
    });

    it('should contain element', function() {

      // given
      expect(editModalPage.variableHelpText()).to.eventually.eql('You can define variables shown in the tasks list.');
      expect(editModalPage.showUndefinedVariablesCheckBox().isSelected()).to.eventually.be.false;
      expect(editModalPage.addVariableButton().isDisplayed()).to.eventually.be.true;

      // when
      editModalPage.showUndefinedVariablesCheckBox().click();

      // then
      expect(editModalPage.showUndefinedVariablesCheckBox().isSelected()).to.eventually.be.true;
    });


    it('should allow to add a variable', function() {

      // when
      editModalPage.addVariableButton().click();

      // then
      expect(editModalPage.variableList().count()).to.eventually.eql(1);
      expect(editModalPage.variableNameInput(0).isEnabled()).to.eventually.be.true;
      expect(editModalPage.variableLabelInput(0).isEnabled()).to.eventually.be.true;
    });


    it('should allow to remove a variable', function() {

      // given
      editModalPage.addVariableButton().click();
      expect(editModalPage.variableList().count()).to.eventually.eql(1);

      // when
      editModalPage.removeVariableButton(0).click();

      // then
      expect(editModalPage.variableList().count()).to.eventually.eql(0);
    });


    it('should clean input fields after removing', function() {

      // given
      editModalPage.addVariable('myName', 'myLabel');
      expect(editModalPage.variableList().count()).to.eventually.eql(1);

      // when
      editModalPage.removeVariableButton(0).click().then(function() {
        editModalPage.addVariableButton().click();
      });

      // then
      expect(editModalPage.variableNameInput(0).getAttribute('value')).to.eventually.eql('');
      expect(editModalPage.variableLabelInput(0).getAttribute('value')).to.eventually.eql('');
    });


    it('should keep my data when playing accordion', function() {

      // given
      editModalPage.addVariable('myName', 'myLabel');
      expect(editModalPage.variableList().count()).to.eventually.eql(1);

      // when
      editModalPage.selectPanelByKey('criteria');
      browser.sleep(500);
      editModalPage.selectPanelByKey('variable');

      // then
      expect(editModalPage.variableNameInput(0).getAttribute('value')).to.eventually.eql('myName');
      expect(editModalPage.variableLabelInput(0).getAttribute('value')).to.eventually.eql('myLabel');
    });

  });


  describe('operate with variables', function() {

    before(function() {
      dashboardPage.navigateTo();
    });

    it('should display variables in the list of task', function() {

      // when
      dashboardPage.taskFilters.selectFilter(1);
      dashboardPage.taskList.taskList().get(0).click();

      // then
      expect(dashboardPage.taskList.taskVariableLabel(0,0).getText()).to.eventually.eql('my test variable:');
      expect(dashboardPage.taskList.taskVariableValue(0,0).getText()).to.eventually.eql('1.5');
      expect(dashboardPage.taskList.taskVariableName(0,0).getText()).to.eventually.eql('myTestVar');
    });


    describe('add additional variable', function() {

      before(function() {
        dashboardPage.taskFilters.selectFilter(1);
        dashboardPage.taskFilters.editFilter(1);
      });

      it('should enter variable data', function() {

        // given
        editModalPage.selectPanelByKey('variable');
        expect(editModalPage.variableList().count()).to.eventually.eql(1);

        // when
        editModalPage.addVariable('myString', 'String Variable');

        // then
        expect(editModalPage.variableList().count()).to.eventually.eql(2);
      });


      it('should save filter and validate results in the list of tasks', function() {

        // when
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskVariableLabel(0,1).getText()).to.eventually.eql('String Variable:');
        expect(dashboardPage.taskList.taskVariableValue(0,1).getText()).to.eventually.eql('123 dfg');
        expect(dashboardPage.taskList.taskVariableName(0,1).getText()).to.eventually.eql('myString');
      });

    });


    describe('display undefined variables', function() {

      before(function() {
        // dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
      });

      it('should select show-undefined option', function() {

        // given
        editModalPage.selectPanelByKey('variable');
        expect(editModalPage.variableList().count()).to.eventually.eql(0);

        // when
        editModalPage.addVariable('MyUndefined', 'undefined Variable');
        editModalPage.showUndefinedVariablesCheckBox().click();

        // then
        expect(editModalPage.variableList().count()).to.eventually.eql(1);
      });


      it('should save filter and see the undefined variable in the list of tasks', function() {

        // when
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskVariableLabel(0,0).getText()).to.eventually.eql('undefined Variable:');
        expect(dashboardPage.taskList.taskVariableValue(0,0).getText()).to.eventually.eql('<Undefined>');
        expect(dashboardPage.taskList.taskVariableName(0,0).getText()).to.eventually.eql('MyUndefined');
      });

    });

  });

});
