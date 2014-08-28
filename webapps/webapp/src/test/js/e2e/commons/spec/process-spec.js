'use strict'

var tasklistPage = require('../../tasklist/pages/dashboard');
var cockpitPage = require('../../cockpit/pages/dashboard');



describe("start process without form", function () {

  describe("start test without start form", function () {

    it("should login to tasklist", function () {

      console.log('\n\n' + 'process spec');

      // when
      tasklistPage.navigateTo();
      tasklistPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      tasklistPage.isActive();
      expect(tasklistPage.startProcess.formElement().isPresent()).toBe(false);
    });

  });


  describe("start instance and check", function () {

    it("should select process to start", function () {

      var processName = 'CallActivity';

      // when
      tasklistPage.startProcess.selectNavbarItem('Process');
      tasklistPage.startProcess.searchProcessInput().sendKeys(processName);
      tasklistPage.startProcess.selectProcessFromSearchResult(0);

      // then
      expect(tasklistPage.startProcess.genericStartForm.processName()).toBe(processName);
    });


    it("should validate generic start form", function () {

      expect(tasklistPage.startProcess.genericStartForm.variableListElement().isPresent()).toBeFalsy();
      expect(tasklistPage.startProcess.genericStartForm.helpText())
          .toBe('You can set process instance variables, using a generic form, by clicking the "Add a variable" link below.');

      // add and remove variable
      tasklistPage.startProcess.genericStartForm.addVariableButton().click();
      expect(tasklistPage.startProcess.genericStartForm.variableListElement().isPresent()).toBeTruthy();
      tasklistPage.startProcess.genericStartForm.removeVariableButton().click();
      expect(tasklistPage.startProcess.genericStartForm.variableListElement().isPresent()).toBeFalsy();

      // start process
      tasklistPage.startProcess.startButton().click();
    });


    it("validate in cockpit", function () {

      // when
      cockpitPage.navigateTo();

      // then
      expect(cockpitPage.deployedProcessesList.runningInstances(2)).toBe('14');
    });

  });


  describe('end test', function() {

    it('should logout', function() {

      // when
      cockpitPage.logout();

      // then
      expect(cockpitPage.authentication.formElement().isPresent()).toBe(true);
    });

  });

});