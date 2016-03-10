'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./filter-basic-setup');

var dashboardPage = require('../pages/dashboard');
var editModalPage = dashboardPage.taskFilters.editFilterPage;


describe('Tasklist Filter Basic Spec', function() {

  describe('initial validation', function() {

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

  });


  describe('create filter', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });

    it('should validate existing filter', function() {

      // then
      expect(dashboardPage.taskFilters.filterName(0)).to.eventually.include('My Tasks');
      expect(dashboardPage.taskFilters.filterName(1)).to.eventually.include('All');
      expect(dashboardPage.taskFilters.isFilterSelected(0)).to.eventually.be.true;
      expect(dashboardPage.taskFilters.isFilterSelected(1)).to.eventually.be.false;
    });


    it('should open create new filter page', function() {

      // when
      dashboardPage.taskFilters.createFilter();

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
      dashboardPage.taskFilters.createFilterPage.saveFilter();

      // then
      expect(dashboardPage.taskFilters.filterName(0)).to.eventually.eql('öäü-Filter');
      expect(dashboardPage.taskFilters.filterDescription(0)).to.eventually.eql('test filter for testing purpose');
    });


    it('should have selected My Task filter after refresh', function() {

      // given
      expect(dashboardPage.taskFilters.isFilterSelected(1)).to.eventually.be.true;

      // when
      browser.getCurrentUrl().then(function (url) {
        browser.get(url).then(function() {
          browser.sleep(500);
        });
      });

      // then
      expect(dashboardPage.taskFilters.isFilterSelected(1)).to.eventually.be.true;
    });


    it('should validate new filter results', function() {

      // when
      dashboardPage.taskFilters.selectFilter(0);

      // then
      expect(dashboardPage.taskFilters.filterName(0)).to.eventually.include('öäü-Filter');
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(3);
    });

  });


  describe('delete filter', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });

    it('should open delete page', function() {

      // when
      dashboardPage.taskFilters.selectFilter(1);
      dashboardPage.taskFilters.editFilter(1);
      dashboardPage.taskFilters.editFilterPage.deleteFilterButton().click();

      // then
      expect(dashboardPage.taskFilters.deleteFilterPage.formHeader()).to.eventually.eql('Delete filter');
      expect(dashboardPage.taskFilters.deleteFilterPage.deleteButton().isPresent()).to.eventually.be.true;
      expect(dashboardPage.taskFilters.deleteFilterPage.closeButton().isPresent()).to.eventually.be.true;
      expect(dashboardPage.taskFilters.deleteFilterPage.editFilterButton().isPresent()).to.eventually.be.true;
    });


    it('should go back to edit page', function() {

      // when
      dashboardPage.taskFilters.deleteFilterPage.editFilterButton().click()

      // then
      expect(dashboardPage.taskFilters.editFilterPage.formHeader()).to.eventually.eql('Edit filter');
    });


    it('should delete the filter', function() {

      // when
      dashboardPage.taskFilters.editFilterPage.deleteFilterButton().click();
      dashboardPage.taskFilters.deleteFilterPage.deleteButton().click();

      // then
      expect(dashboardPage.taskFilters.filterList().count()).to.eventually.eql(2);
      expect(dashboardPage.taskFilters.filterName(1)).not.to.eventually.eql('My new Filter');
      expect(dashboardPage.taskFilters.isFilterSelected(0)).to.eventually.be.true;
    });


    it('should cancel filter deletion', function() {

      // when
      dashboardPage.taskFilters.selectFilter(1);
      dashboardPage.taskFilters.editFilter(1);
      dashboardPage.taskFilters.editFilterPage.deleteFilterButton().click();
      dashboardPage.taskFilters.editFilterPage.closeButton().click();

      // then
      expect(dashboardPage.taskFilters.filterList().count()).to.eventually.eql(2);
      expect(dashboardPage.taskFilters.filterName(1)).to.eventually.include('Test Filter');
      expect(dashboardPage.taskFilters.isFilterSelected(1)).to.eventually.be.true;
    });

  });


  describe('edit filter', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });

    it('should open edit menu of My Tasks filter', function() {

      // when
      dashboardPage.taskFilters.selectFilter(0);
      dashboardPage.taskFilters.editFilter(0);

      // then
      expect(editModalPage.formHeader()).to.eventually.eql('Edit filter');
      expect(editModalPage.saveButton().isPresent()).to.eventually.be.true;
      expect(editModalPage.closeButton().isPresent()).to.eventually.be.true;
      expect(editModalPage.deleteFilterButton().isPresent()).to.eventually.be.true;
    });


    it('should save changes', function() {

      // when
      editModalPage.nameInput(' are shown here');
      editModalPage.descriptionInput('Show all my Tasks');
      editModalPage.priorityInput().clear();
      editModalPage.priorityInput(111);
      editModalPage.saveFilter();

      // then
      expect(dashboardPage.taskFilters.filterName(2)).to.eventually.include('My Tasks are shown here');
      expect(dashboardPage.taskFilters.filterDescription(2)).to.eventually.eql('Show all my Tasks');
      expect(dashboardPage.taskFilters.isFilterSelected(2)).to.eventually.be.true;
    });

  });

});
