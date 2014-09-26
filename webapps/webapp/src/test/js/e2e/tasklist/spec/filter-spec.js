'use strict';

var dashboardPage = require('../pages/dashboard');

describe('tasklist filter -', function() {

  describe("start test", function () {

    it('should login', function () {

      // when
      dashboardPage.navigateTo();
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      dashboardPage.isActive();
    });

  });


  xdescribe("validate filter page", function () {

  });


  describe("create new filter", function () {

    it("should open create new filter page", function () {

      // when
      dashboardPage.taskFilters.createFilterButton().click();

      // then
      expect(dashboardPage.taskFilters.createFilterPage.formHeader()).toBe('Create a filter');
      expect(dashboardPage.taskFilters.createFilterPage.saveButton().isEnabled()).toBe(true);
      expect(dashboardPage.taskFilters.createFilterPage.closeButton().isPresent()).toBe(true);
    });


    it("should enter filter values", function () {

      // when
      dashboardPage.taskFilters.createFilterPage.nameInput('ÖÄÜ-FILTER');
      dashboardPage.taskFilters.createFilterPage.priorityInput().clear();
      dashboardPage.taskFilters.createFilterPage.priorityInput(-200);
      dashboardPage.taskFilters.createFilterPage.descriptionInput('test filter for testing purpose');
      dashboardPage.taskFilters.createFilterPage.saveButton().click();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).toBe('ÖÄÜ-FILTER');
      expect(dashboardPage.taskFilters.filterDescription(0)).toBe('test filter for testing purpose');
    });


    it("should validate filter results", function () {

      expect(dashboardPage.taskList.taskList().count()).toBeGreaterThan(0);
    });

  });


  describe("edit filter", function () {

    it("should open edit filter page", function () {

      // when
      dashboardPage.taskFilters.editFilter(0);

      // then
      expect(dashboardPage.taskFilters.editFilterPage.formHeader()).toBe('Edit filter');
      expect(dashboardPage.taskFilters.editFilterPage.saveButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.editFilterPage.closeButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.editFilterPage.deleteFilterButton().isPresent()).toBe(true);
    });


    it("should play accordion", function () {

      dashboardPage.taskFilters.editFilterPage.selectPanel('Criteria');
      dashboardPage.taskFilters.editFilterPage.selectPanel('General');
      dashboardPage.taskFilters.editFilterPage.selectPanel('Variables');
      dashboardPage.taskFilters.editFilterPage.selectPanel('Authorizations');

      dashboardPage.taskFilters.editFilterPage.closeButton().click();
    });


    describe("variables", function () {

      it("should open variables panel", function () {

        // when
        dashboardPage.taskFilters.editFilter(0);
        dashboardPage.taskFilters.editFilterPage.selectPanel('Variables');

        // then
        expect(dashboardPage.taskFilters.editFilterPage.variableList().count()).toBe(0);
      });

      it("should add a variable", function () {
        var variableName = 'ÖÄÜ-123';
        var varibleLabel = '123 blaw';

        // when
        dashboardPage.taskFilters.editFilterPage.addVariableButton().click();
        dashboardPage.taskFilters.editFilterPage.variableNameInput(0,variableName);
        dashboardPage.taskFilters.editFilterPage.variableLabelInput(0, varibleLabel);

        // then
        dashboardPage.taskFilters.editFilterPage.selectPanel('General');
        dashboardPage.taskFilters.editFilterPage.selectPanel('Variables');
        dashboardPage.taskFilters.editFilterPage.saveButton().click();
      });

    });


    describe("criteria", function () {

      it("should add new criteria", function () {

        // when
        dashboardPage.taskFilters.editFilter(0);
        dashboardPage.taskFilters.editFilterPage.selectPanel('Criteria');
        dashboardPage.taskFilters.editFilterPage.addCriterionButton().click();

        // then
        dashboardPage.taskFilters.editFilterPage.keyInput('Id');
        dashboardPage.taskFilters.editFilterPage.valueInput('4711');

        dashboardPage.taskFilters.editFilterPage.saveButton().click();
      });


      it("should validate filter results", function () {

        // when
        dashboardPage.taskFilters.selectFilter(0);

        // then
        expect(dashboardPage.taskList.taskList().count()).toBe(0);
      });

    });

  });


  describe("delete filter", function () {

    it("should open delete page", function () {

      // when
      dashboardPage.taskFilters.deleteFilter(0);

      // then
      expect(dashboardPage.taskFilters.deleteFilterPage.formHeader()).toBe('Delete filter');
      expect(dashboardPage.taskFilters.deleteFilterPage.deleteButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.deleteFilterPage.closeButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.deleteFilterPage.editFilterButton().isPresent()).toBe(true);
    });


    it("should delete the filter", function () {

      // when
      dashboardPage.taskFilters.deleteFilterPage.deleteButton().click();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).not.toBe('ÖÄÜ-FILTER');
    });

  });


  describe('end test', function() {

    it('should logout', function() {

      dashboardPage.navigateLogout();
    });

  });

});