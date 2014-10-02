'use strict';

var dashboardPage = require('../pages/dashboard');

describe('tasklist filter -', function() {

  describe("start test", function() {

    it('should login', function() {

      // when
      dashboardPage.navigateTo();
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      dashboardPage.isActive();
    });

  });


  describe("create new filter", function() {

    it("should open create new filter page", function() {

      // when
      dashboardPage.taskFilters.createFilterButton().click();

      // then
      expect(dashboardPage.taskFilters.createFilterPage.formHeader()).toBe('Create a filter');
      expect(dashboardPage.taskFilters.createFilterPage.saveButton().isEnabled()).toBe(true);
      expect(dashboardPage.taskFilters.createFilterPage.closeButton().isPresent()).toBe(true);
    });


    it("should enter filter values", function() {

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


    it("should validate filter results", function() {

      expect(dashboardPage.taskList.taskList().count()).toBeGreaterThan(0);
    });

  });


  describe("edit filter", function() {

    it("should edit first filter", function() {

      // when
      dashboardPage.taskFilters.editFilter(0);

      // then
      expect(dashboardPage.taskFilters.editFilterPage.formHeader()).toBe('Edit filter');
      expect(dashboardPage.taskFilters.editFilterPage.saveButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.editFilterPage.closeButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.editFilterPage.deleteFilterButton().isPresent()).toBe(true);
    });


    describe("criteria panel", function() {

      it("should open criteria panel", function() {

        // when
        dashboardPage.taskFilters.editFilterPage.selectPanel('Criteria');

        // then
        expect(dashboardPage.taskFilters.editFilterPage.addCriterionButton().isPresent()).toBe(true);
      });


      it("should add new criteria", function() {

        // when
        dashboardPage.taskFilters.editFilterPage.addCriteria('Id', '4711');

        // then
        expect(dashboardPage.taskFilters.editFilterPage.criterionList().count()).toBe(1);
      });

    });


    describe("variable panel", function() {

      it("should open variables panel", function() {

        // when
        dashboardPage.taskFilters.editFilterPage.selectPanel('Variables');

        // then
        expect(dashboardPage.taskFilters.editFilterPage.variableList().count()).toBe(0);
      });


      it("should add a variable", function() {

        // when
        dashboardPage.taskFilters.editFilterPage.addVariable('ÖÄÜ-123', '123 blaw');

        // then
        expect(dashboardPage.taskFilters.editFilterPage.variableList().count()).toBe(1);
      });

    });


    xdescribe("authorization panel", function() {

    });


    describe("general panel", function() {

      it("should open general panel", function() {

        // when
        dashboardPage.taskFilters.editFilterPage.selectPanel('General');

        // then
        expect(dashboardPage.taskFilters.editFilterPage.nameInput().getAttribute('value')).toEqual('ÖÄÜ-FILTER');
      });


      it("should change general panel value", function() {

        // when
        dashboardPage.taskFilters.editFilterPage.nameInput().clear();
        dashboardPage.taskFilters.editFilterPage.nameInput('123 Filter Name');

        dashboardPage.taskFilters.editFilterPage.priorityInput().clear();
        dashboardPage.taskFilters.editFilterPage.priorityInput(-9);

        // then
        // check in 'should validate priority ranking'
      });

    });


    it("should save filter", function() {

      // when
      dashboardPage.taskFilters.editFilterPage.saveButton().click();

      browser.sleep(2000);

      // then
      expect(dashboardPage.taskFilters.editFilterPage.formElement().isPresent()).toBe(false);
    });


    it("should validate priority ranking", function() {

      expect(dashboardPage.taskFilters.filterName(1)).toBe('123 FILTER NAME');
    });


    it("should validate filter refresh", function() {

      expect(dashboardPage.taskList.taskList().count()).toBe(0);

      expect(dashboardPage.taskFilters.isFilterSelected(1));
    });

  });


  describe("use filter", function() {

    it("should show filter results after relogin", function () {

      // when
      dashboardPage.taskFilters.selectFilter(0);
      var taskName = dashboardPage.taskList.taskTitle(0);

      dashboardPage.navigateLogout();
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      expect(dashboardPage.taskList.taskTitle(0)).toBe(taskName);
    });

  });


  describe("delete filter", function() {

    it("should open delete page", function() {

      // when
      dashboardPage.taskFilters.deleteFilter(1);

      // then
      expect(dashboardPage.taskFilters.deleteFilterPage.formHeader()).toBe('Delete filter');
      expect(dashboardPage.taskFilters.deleteFilterPage.deleteButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.deleteFilterPage.closeButton().isPresent()).toBe(true);
      expect(dashboardPage.taskFilters.deleteFilterPage.editFilterButton().isPresent()).toBe(true);
    });


    it("should delete the filter", function() {

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
