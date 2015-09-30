'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/process-definition');
var instancePage = require('../pages/process-instance');

describe('Cockpit Variable Spec', function() {

  describe('work with variables', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.selectInstanceId(0);
      });
    });


    // What is that???
    xit('should select variable scope', function() {

      // given
      expect(instancePage.instanceTree.instanceSelectionLabel().getText()).to.eventually.eql('Nothing');

      // when
      instancePage.variablesTab.variableScope(1).click();

      // then
      expect(instancePage.instanceTree.isInstanceSelected('User Tasks')).to.eventually.be.true;
      expect(instancePage.instanceTree.instanceSelectionLabel().getText()).to.eventually.eql('1 activity instance');

      // finaly
      instancePage.instanceTree.clearInstanceSelection();
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
          expect(instancePage.variablesTab.variableName(idx).getText()).to.eventually.eql('myTestVar');
          expect(instancePage.variablesTab.variableType(idx).getText()).to.eventually.eql('String');
          expect(instancePage.variablesTab.variableValue(idx).getText()).to.eventually.eql('12345');
          expect(instancePage.variablesTab.variableScope(idx).getText()).to.eventually.eql('User Tasks');
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
            expect(instancePage.variablesTab.variableName(idx).getText()).to.eventually.eql('myBooleanVar');
            expect(instancePage.variablesTab.variableType(idx).getText()).to.eventually.eql('Boolean');
            expect(instancePage.variablesTab.variableValue(idx).getText()).to.eventually.eql('true');
            expect(instancePage.variablesTab.variableScope(idx).getText()).to.eventually.eql('User Tasks');
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
          expect(instancePage.variablesTab.variableName(idx).getText()).to.eventually.eql('myNullVar');
          expect(instancePage.variablesTab.variableType(idx).getText()).to.eventually.eql('Null');
          expect(instancePage.variablesTab.variableValue(idx).getText()).to.eventually.eql('');
          expect(instancePage.variablesTab.variableScope(idx).getText()).to.eventually.eql('User Tasks');
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
          expect(instancePage.variablesTab.variableName(idx).getText()).to.eventually.eql('myObjectVar');
          expect(instancePage.variablesTab.variableType(idx).getText()).to.eventually.eql('Object');
          expect(instancePage.variablesTab.variableValue(idx).getText()).to.eventually.eql('java.lang.Object');
          expect(instancePage.variablesTab.variableScope(idx).getText()).to.eventually.eql('User Tasks');
        });
      });
    });


    it('should change variable', function() {

      // given
      instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'test')
        .then(function(idx) {
          expect(instancePage.variablesTab.variableType(idx).getText()).to.eventually.eql('Double');
          expect(instancePage.variablesTab.variableValue(idx).getText()).to.eventually.eql('1.49');

          // when
          instancePage.variablesTab.editVariableButton(idx).click().then(function() {
            instancePage.variablesTab.editVariableInput().clear();
            instancePage.variablesTab.editVariableInput('1.5');
            instancePage.variablesTab.editVariableType('String');
            instancePage.variablesTab.editVariableConfirmButton().click();
          });

          // then
          expect(instancePage.variablesTab.variableName(idx).getText()).to.eventually.eql('test');
          expect(instancePage.variablesTab.variableValue(idx).getText()).to.eventually.eql('1.5');
          expect(instancePage.variablesTab.variableType(idx).getText()).to.eventually.eql('String');
        });
    });


    it('should select wrong variable type', function() {

      // given
      instancePage.variablesTab.findElementIndexInRepeater('variable in variables', by.binding('variable.name'), 'myDate')
        .then(function(idx) {
          expect(instancePage.variablesTab.variableType(idx).getText()).to.eventually.eql('Date');
          expect(instancePage.variablesTab.variableValue(idx).getText()).to.eventually.eql('2011-11-11T11:11:11');

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
      // ensure the modal dialog to be closed
      after(function (done) {
        var btn = instancePage.addVariable.okButton();
        btn.isPresent().then(function () {
          btn.click().then(function () {done();});
        }, function () {done();});
      });


      it('should open modal view', function() {

        // when
        instancePage.addVariable.addVariableButton().click();

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


    describe('variables table widget', function () {
      var variable;

      before(function () {
        variable = instancePage.variablesTab.variableAt(0);
      });


      it('shows the information about the variables by default', function () {
        expect(variable.name().getText()).to.eventually.eql('myString');

        expect(variable.value().getText()).to.eventually.eql('abc dfg');

        expect(variable.type().getText()).to.eventually.eql('String');

        expect(variable.actionsCell().isDisplayed()).to.eventually.eql(true);
      });


      it('provides actions', function () {
        expect(variable.editButton().isDisplayed()).to.eventually.eql(true);

        expect(variable.deleteButton().isDisplayed()).to.eventually.eql(true);
      });


      describe('edit mode', function () {
        before(function () {
          variable.enterEditMode();
        });


        it('shows the relevant fields', function () {
          expect(variable.typeSelectElement().isDisplayed()).to.eventually.eql(true);

          expect(variable.valueInput().isDisplayed()).to.eventually.eql(true);

          expect(variable.nameInput().isPresent()).to.eventually.eql(false);
        });


        describe('unchanged', function () {
          it('does not allow to save', function () {
            expect(variable.saveButton().getAttribute('disabled')).to.eventually.eql('true');
          });
        });


        describe('changed', function () {
          before(function () {
            variable.valueInput().clear().sendKeys('pipapo');
          });


          it('enables the save button', function () {
            expect(variable.saveButton().getAttribute('disabled')).to.eventually.eql(null);
          });


          describe('save action', function () {
            before(function () {
              variable.saveButton().click();
            });


            it('saves the variable', function () {
              expect(variable.value().getText()).to.eventually.eql('pipapo');
            });


            it('exits the edit mode', function () {
              expect(variable.typeSelectElement().isPresent()).to.eventually.eql(false);

              expect(variable.valueInput().isPresent()).to.eventually.eql(false);

              expect(variable.nameInput().isPresent()).to.eventually.eql(false);

              expect(variable.saveButton().isPresent()).to.eventually.eql(false);

              expect(variable.editButton().isDisplayed()).to.eventually.eql(true);
            });
          });
        });
      });


      describe('deletion', function () {
        before(function () {
          instancePage.addVariable.addVariable('toBeDeleted', 'String', 'whatever');

          variable = instancePage.variablesTab.variableByName('toBeDeleted');
        });


        it('is possible in read mode', function () {
          expect(variable.name().getText()).to.eventually.eql('toBeDeleted');

          expect(variable.deleteButton().isDisplayed()).to.eventually.eql(true);

          variable.deleteButton().click();

          expect(variable.node.isPresent()).to.eventually.eql(false);
        });
      });
    });
  });

});
