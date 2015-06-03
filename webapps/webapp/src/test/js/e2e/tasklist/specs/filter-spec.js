'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./filter-setup');

var dashboardPage = require('../pages/dashboard');
var editModalPage = dashboardPage.taskFilters.editFilterPage;

describe.skip('Tasklist Filter Spec', function() {

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
/*      browser.executeScript("$('.modal').removeClass('fade');");

      // when
      dashboardPage.taskFilters.createFilterButton().click().then(function() {
        browser.waitForAngular();

        // then
        expect(dashboardPage.taskFilters.createFilterPage.formHeader()).to.eventually.eql('Create a filter');
        expect(dashboardPage.taskFilters.createFilterPage.saveButton().isEnabled()).to.eventually.be.false;
        expect(dashboardPage.taskFilters.createFilterPage.closeButton().isPresent()).to.eventually.be.true;
      });*/
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
      expect(dashboardPage.taskFilters.filterName(0)).to.eventually.eql('öäü-Filter');
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(5);
    });

  });


  describe('edit filter', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');

        dashboardPage.taskList.taskSorting.changeSorting(0, 'Task name');
      });
    });


    describe('general panel', function() {

      it('should edit general panel of My Filter', function() {

        // when
        dashboardPage.taskFilters.selectFilter(1);  // check filter is selected after edit
        dashboardPage.taskFilters.editFilter(0);

        // then
        expect(editModalPage.formHeader()).to.eventually.eql('Edit filter');
        expect(editModalPage.saveButton().isPresent()).to.eventually.be.true;
        expect(editModalPage.closeButton().isPresent()).to.eventually.be.true;
        expect(editModalPage.deleteFilterButton().isPresent()).to.eventually.be.true;
      });


      it('should change general panel value', function() {

        // when
        editModalPage.nameInput().clear();
        editModalPage.nameInput('My new Filter');

        dashboardPage.taskFilters.createFilterPage.descriptionInput('"§$%&/()=?öäü!');

        editModalPage.priorityInput().clear();
        editModalPage.priorityInput(101);
      });


      it('should save filter', function() {

        // when
        editModalPage.saveButton().click();

        // then
        expect(editModalPage.formElement().isPresent()).to.eventually.be.false;
        expect(dashboardPage.taskFilters.filterName(1)).to.eventually.eql('My new Filter');
        // expect(dashboardPage.taskFilters.filterDescription(1)).to.eventually.eql('"§$%&/()=?öäü!');
        expect(dashboardPage.taskFilters.isFilterSelected(0)).to.eventually.be.true;
      });

    });


    describe('variable panel', function() {

      before(function() {
        dashboardPage.navigateTo();
        dashboardPage.taskFilters.selectFilter(2);
        return dashboardPage.taskFilters.editFilter(2);
      });

      it('should edit variable panel of Test Filter', function() {

        // when
        expect(editModalPage.selectPanelByKey('variable')).to.eventually.be.true;

        // then
        expect(editModalPage.variableList().count()).to.eventually.eql(0);
        expect(editModalPage.showUndefinedVariablesCheckBox().isSelected()).to.eventually.be.false;
      });


      it('should add variables', function() {

        // when
        editModalPage.addVariable('testString', '__String//Variable');
        editModalPage.saveButton().click();

        // then
        expect(dashboardPage.taskList.taskVariableLabel(3,0).getText()).to.eventually.eql('__String//Variable:');
        expect(dashboardPage.taskList.taskVariableName(3,0)).to.eventually.eql('testString');
        expect(dashboardPage.taskList.taskVariableValue(3,0).getText()).to.eventually.eql('asdfhans dampf');
        expect(dashboardPage.taskFilters.isFilterSelected(2)).to.eventually.be.true;
      });

    });


    describe('criteria panel', function() {

      beforeEach(function() {
        dashboardPage.navigateTo();
        dashboardPage.taskFilters.selectFilter(2);
        return dashboardPage.taskFilters.editFilter(2);
      });


      it('should add process-definition-key criteria', function() {

        // given
        expect(editModalPage.selectPanelByKey('criteria')).to.eventually.be.true;
        expect(editModalPage.addCriterionButton().isPresent()).to.eventually.be.true;

        // when
        editModalPage.addCriteria('Process definition', 'Key', 'user-tasks');
        editModalPage.saveButton().click();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
      });


      it('should add Business Key Like criteria', function() {

        // given
        expect(editModalPage.selectPanelByKey('criteria')).to.eventually.be.true;
        expect(editModalPage.addCriterionButton().isPresent()).to.eventually.be.true;

        // when
        editModalPage.addCriteria('Process Instance', 'Business Key Like', 'myBus');
        editModalPage.saveButton().click();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
      });


      it('should remove process-definition-key criteria', function() {

        // given
        expect(editModalPage.selectPanelByKey('criteria')).to.eventually.be.true;
        expect(editModalPage.addCriterionButton().isPresent()).to.eventually.be.true;

        // when
        editModalPage.removeCriterionButton(0).click();
        editModalPage.saveButton().click();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
      });


      it('should change first criteria to Assignee Like', function() {

        // given
        expect(editModalPage.selectPanelByKey('criteria')).to.eventually.be.true;
        expect(editModalPage.addCriterionButton().isPresent()).to.eventually.be.true;

        // when
        editModalPage.editCriteria(0, 'User / Group', 'Assignee Like\n', 'est');
        editModalPage.saveButton().click();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 2');
      });


      describe('the includeAssignedTasks option', function () {

        it('should be shown depending on the criteria', function() {

          //given
          var checkbox = editModalPage.includeAssignedTasksCheckbox();
          expect(editModalPage.selectPanelByKey('criteria')).to.eventually.be.true;

          // when
          editModalPage.editCriteria(0, 'User / Group', 'Candidate Group\n', 'bla');

          // then
          expect(checkbox.isDisplayed()).to.eventually.be.true;
          expect(checkbox.isSelected()).to.eventually.be.false;

          // when
          editModalPage.editCriteria(0, 'User / Group', 'Candidate Groups', 'bla');

          // then
          expect(checkbox.isDisplayed()).to.eventually.be.true;
          expect(checkbox.isSelected()).to.eventually.be.false;

          // when
          editModalPage.editCriteria(0, 'User / Group', 'Candidate User', 'bla');

          // then
          expect(checkbox.isDisplayed()).to.eventually.be.true;
          expect(checkbox.isSelected()).to.eventually.be.false;

          // when
          editModalPage.editCriteria(0, 'User / Group', 'Assignee Like\n', 'bla');

          // then
          expect(checkbox.isPresent()).to.eventually.be.false;
        });


        it('should be used with Candidate Groups criteria', function () {

          // given
          expect(editModalPage.selectPanelByKey('criteria')).to.eventually.be.true;

          // when
          editModalPage.editCriteria(0, 'User / Group', 'Candidate Groups\n', 'management ,Accounting');
          editModalPage.saveButton().click();

          // then
          expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
          expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
          expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('User Task 1');
        });


        it('should filter unassigneed tasks only if not selected', function() {

          // given
          editModalPage.closeButton().click();

          // when
          dashboardPage.taskList.selectTask(0);
          dashboardPage.currentTask.claim();

          // then
          expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
          expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
        });


        it('should filter assigned tasks too if selected', function() {

          // given
          expect(editModalPage.selectPanelByKey('criteria')).to.eventually.be.true;
          browser.sleep(500);

          // when
          editModalPage.includeAssignedTasksCheckbox().click();
          editModalPage.saveButton().click();

          // then
          expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
          expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
          expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('User Task 1');
        });

      });

    });

  });


  describe('delete filter', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });


    it('should open delete page', function() {

      // when
      dashboardPage.taskFilters.selectFilter(1);
      dashboardPage.taskFilters.editFilter(1);
      editModalPage.deleteFilterButton().click();

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
      expect(dashboardPage.taskFilters.filterList().count()).to.eventually.eql(2);
      expect(dashboardPage.taskFilters.filterName(1)).not.to.eventually.eql('My new Filter');
      expect(dashboardPage.taskFilters.isFilterSelected(0)).to.eventually.be.true;
    });

  });

});
