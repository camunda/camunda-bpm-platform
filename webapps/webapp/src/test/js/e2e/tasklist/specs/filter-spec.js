'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./filter-setup');

var dashboardPage = require('../pages/dashboard');

describe('Tasklist Filter Spec', function() {

  before(function() {
    return testHelper(function() {

      dashboardPage.navigateToWebapp('Tasklist');
      dashboardPage.authentication.userLogin('admin', 'admin');
    });
  });


  it('should validate default conditions', function() {

    // then
    expect(dashboardPage.taskFilters.filterListInfoText()).to.eventually.eql('No filter available.');
    expect(dashboardPage.taskList.taskListInfoText()).to.eventually.eql('No task matching filters found.');
  });


  describe('create new filter', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });


    it('should validate existing filter', function() {

      // then
      expect(dashboardPage.taskFilters.filterName(0)).to.eventually.eql('My Tasks');
      expect(dashboardPage.taskFilters.filterName(1)).to.eventually.eql('All');
      expect(dashboardPage.taskFilters.isFilterSelected(0)).to.eventually.be.true;
      expect(dashboardPage.taskFilters.isFilterSelected(1)).to.eventually.be.false;
    });


    it('should open create new filter page', function() {

      // when
      dashboardPage.taskFilters.createFilterButton().click();

      // then
      expect(dashboardPage.taskFilters.createFilterPage.formHeader()).to.eventually.eql('Create a filter');
      expect(dashboardPage.taskFilters.createFilterPage.saveButton().isEnabled()).to.eventually.be.false;
      expect(dashboardPage.taskFilters.createFilterPage.closeButton().isPresent()).to.eventually.be.true;
    });


    it('should enter filter values', function() {

      // when
      dashboardPage.taskFilters.createFilterPage.nameInput('öäü-Filter');
      dashboardPage.taskFilters.createFilterPage.priorityInput().clear();
      dashboardPage.taskFilters.createFilterPage.priorityInput(-200);
      dashboardPage.taskFilters.createFilterPage.descriptionInput('test filter for testing purpose');
      dashboardPage.taskFilters.createFilterPage.saveButton().click();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).to.eventually.eql('öäü-Filter');
      expect(dashboardPage.taskFilters.filterDescription(0)).to.eventually.eql('test filter for testing purpose');
    });


    it('should validate filter results', function() {

      // then
      expect(dashboardPage.taskFilters.isFilterSelected(1)).to.eventually.be.true;
      expect(dashboardPage.taskFilters.filterName(0)).to.eventually.eql('öäü-Filter');
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
    });

  });


  /*describe('edit filter', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });

    it('should edit first filter', function() {

      // when
      dashboardPage.taskFilters.selectFilter(0);  // check filter is selected after edit
      dashboardPage.taskFilters.editFilter(0);

      // then
      expect(dashboardPage.taskFilters.editFilterPage.formHeader()).to.eventually.eql('Edit filter');
      expect(dashboardPage.taskFilters.editFilterPage.saveButton().isPresent()).to.eventually.be.true;
      expect(dashboardPage.taskFilters.editFilterPage.closeButton().isPresent()).to.eventually.be.true;
      expect(dashboardPage.taskFilters.editFilterPage.deleteFilterButton().isPresent()).to.eventually.be.true;
    });


    it('should open criteria panel', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.selectPanel('Criteria');

      // then
      expect(dashboardPage.taskFilters.editFilterPage.addCriterionButton().isPresent()).to.eventually.be.true;
    });


    it('should add new criteria', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.addCriteria('Id', '4711');

      // then
      expect(dashboardPage.taskFilters.editFilterPage.criterionList().count()).to.eventually.eql(1);
    });


    it('should open variables panel', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.selectPanel('Variables');

      // then
      expect(dashboardPage.taskFilters.editFilterPage.variableList().count()).to.eventually.eql(0);
    });


    it('should add a variable', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.addVariable('ÖÄÜ-123', '123 blaw');

      // then
      expect(dashboardPage.taskFilters.editFilterPage.variableList().count()).to.eventually.eql(1);
    });


    it('should open general panel', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.selectPanel('General');

      // then
      expect(dashboardPage.taskFilters.editFilterPage.nameInput().getAttribute('value')).toEqual('ÖÄÜ-FILTER');
    });


    it('should change general panel value', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.nameInput().clear();
      dashboardPage.taskFilters.editFilterPage.nameInput('123 Filter Name');

      dashboardPage.taskFilters.editFilterPage.priorityInput().clear();
      dashboardPage.taskFilters.editFilterPage.priorityInput(10);

      // then
      // check in 'should validate priority ranking'
    });



    it('should save filter', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.saveButton().click();

      browser.sleep(2000);

      // then
      expect(dashboardPage.taskFilters.editFilterPage.formElement().isPresent()).to.eventually.eql(false);
    });


    it('should validate priority ranking', function() {

      // then
      expect(dashboardPage.taskFilters.filterName(1)).to.eventually.eql('123 Filter Name');
    });


    it('should validate filter refresh', function() {

      // then
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(0);
      expect(dashboardPage.taskFilters.isFilterSelected(1));
    });

  });*/

/*
  describe('use filter', function() {

    it('should show filter results after relogin', function () {

      // when
      dashboardPage.taskFilters.selectFilter(0);
      var taskName = dashboardPage.taskList.taskName(0);

      dashboardPage.navigateLogout();
      dashboardPage.authentication.userLogin('jonny1', 'jonny1');

      // then
      expect(dashboardPage.taskList.taskName(0)).to.eventually.eql(taskName);
    });

  });


  describe('delete filter', function() {

    it('should open delete page', function() {

      // when
      dashboardPage.taskFilters.editFilter(1);
      dashboardPage.taskFilters.editFilterPage.deleteFilterButton().click();

      // then
      expect(dashboardPage.taskFilters.deleteFilterPage.formHeader()).to.eventually.eql('Delete filter');
      expect(dashboardPage.taskFilters.deleteFilterPage.deleteButton().isPresent()).to.eventually.be.true;
      expect(dashboardPage.taskFilters.deleteFilterPage.closeButton().isPresent()).to.eventually.be.true;
      expect(dashboardPage.taskFilters.deleteFilterPage.editFilterButton().isPresent()).to.eventually.be.true;
    });


    it('should delete the filter', function() {

      // when
      dashboardPage.taskFilters.deleteFilterPage.deleteButton().click();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).not.to.eventually.eql('ÖÄÜ-FILTER');
    });

  });

*/
});

