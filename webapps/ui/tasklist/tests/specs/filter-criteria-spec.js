'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./filter-criteria-setup');

var dashboardPage = require('../pages/dashboard');
var editModalPage = dashboardPage.taskFilters.editFilterPage;


describe('Tasklist Filter Criteria Spec', function() {

  describe('the criteria page', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    beforeEach(function() {
      dashboardPage.taskFilters.selectFilter(0);
      dashboardPage.taskFilters.editFilter(0);
      editModalPage.selectPanelByKey('criteria');
    });

    afterEach(function() {
      editModalPage.closeFilter();
    });

    it('should display help text', function() {

      // then
      expect(editModalPage.criteriaHelpText())
        .to.eventually.eql('This section is aimed to set the parameters used to filter the tasks. Keys marked with a * accept expressions as value.')
      expect(editModalPage.addCriterionButton().isDisplayed()).to.eventually.be.true;
    });


    it('should allow to add criteria', function() {

      // when
      editModalPage.addCriterionButton().click();

      // then
      expect(editModalPage.criterionList().count()).to.eventually.eql(1);
      expect(editModalPage.criterionKeyInput(0).isEnabled()).to.eventually.be.true;
      expect(editModalPage.criterionValueInput(0).isEnabled()).to.eventually.be.true;
    });


    it('should allow to remove criteria', function() {

      // given
      editModalPage.addCriterionButton().click();
      expect(editModalPage.criterionList().count()).to.eventually.eql(1);

      // when
      editModalPage.removeCriterionButton(0).click();

      // then
      expect(editModalPage.criterionList().count()).to.eventually.eql(0);
    });


    it('should clean input fields after removing', function() {

      // given
      editModalPage.addCriterion('Task', 'Name Like', 'task');

      // when
      editModalPage.removeCriterionButton(0).click();
      editModalPage.addCriterionButton().click();

      // then
      expect(editModalPage.criterionKeyInput(0).getAttribute('value')).to.eventually.eql('? undefined:undefined ?');
      expect(editModalPage.criterionValueInput(0).getAttribute('value')).to.eventually.eql('');
    });


    it('should validate input - missing criterion', function() {

      // when
      editModalPage.addCriterionButton().click();
      editModalPage.criterionValueInput(0, 'myValue');

      // then
      expect(editModalPage.saveButton().isEnabled()).to.eventually.be.false;
    });


    it('should validate input - missing value', function() {

      // when
      editModalPage.addCriterionButton().click();
      editModalPage.selectCriterionKey(0, 'Case Instance', 'Business Key Like');

      // then
      expect(editModalPage.saveButton().isEnabled()).to.eventually.be.false;
    });


    it('should validate input - unique criterion key', function() {

      // when
      editModalPage.addCriterion('Process Instance', 'ID', 'the value');
      editModalPage.addCriterion('Process Instance', 'ID', 'the other value');

      // then
      expect(editModalPage.criterionKeyHelpText(0)).to.eventually.eql('Key must be unique');
      expect(editModalPage.criterionKeyHelpText(1)).to.eventually.eql('Key must be unique');
      expect(editModalPage.saveButton().isEnabled()).to.eventually.be.false;
    });


    it('should validate input - delegation state key', function() {

      var criterionValue = 'the wrong delegation state value';

      // when
      editModalPage.addCriterion('User / Group', 'Delegation State', criterionValue);
      editModalPage.saveButton().click();

      // then
      expect(editModalPage.notificationStatus(0))
        .to.eventually.eql('Something went wrong while saving the filter');
      expect(editModalPage.notificationMessage(0))
        .to.eventually.eql('Valid values for property \'delegationState\' are \'PENDING\' or \'RESOLVED\', but was \'' +
          criterionValue + '\'');
    });

  });


  describe('use criteria', function() {

    describe('to filter by instance, definition and task', function() {

      before(function() {
        return testHelper(setupFile.setup1, function() {
          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('test', 'test');
          dashboardPage.taskList.taskSorting.changeSorting(0, 'Task name');
        });
      });

      beforeEach(function() {
        dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
        editModalPage.selectPanelByKey('criteria');
      });

      it('should add task name like criterion and validate result', function() {

        // when
        editModalPage.addCriterion('Task', 'Name Like', 'ask');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(3);
      });


      it('should edit task name like criterion value and validate result', function() {

        // when
        editModalPage.criterionValueInput(0, ' 1');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
      });


      it('should add process name criterion and validate results', function() {

        // when
        editModalPage.addCriterion('Process definition', 'Key', 'user-tasks');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
      });

      it('should add business key criterion and validate results', function() {

        // when
        editModalPage.addCriterion('Process Instance', 'Business Key', 123);
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
      });

    });


    describe('to filter by User / Groups', function() {

      before(function() {
        return testHelper(setupFile.setup1, function() {
          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('test', 'test');
          dashboardPage.taskList.taskSorting.changeSorting(0, 'Task name');
        });
      });

      beforeEach(function() {
        dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
        editModalPage.selectPanelByKey('criteria');
      });

      it('should add Candidate Groups criterion and validate result', function() {

        // when
        editModalPage.addCriterion('User / Group', 'Candidate Groups', '${ currentUserGroups() }');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 1');
      });


      it('should edit criterion value and validate result', function() {

        // when
        editModalPage.editCriterion(0, 'User / Group', 'Candidate Groups', 'accounting,sales');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
        expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('Task 1');
      });


      it('should include assigned tasks and validate result', function() {

        // when
        editModalPage.includeAssignedTasksCheckbox().click();
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('User Task 1');
        expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('Task 2');
        expect(dashboardPage.taskList.taskName(2)).to.eventually.eql('Task 1');
      });


      it('should add second criterion to filter by owner and validate result', function() {

        // when
        editModalPage.addCriterion('User / Group', 'Owner', 'test');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 2');
        expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('Task 1');
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
      });

    });


    describe('to filter by dates', function() {

      before(function() {
        return testHelper(setupFile.setup1, function() {

          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('test', 'test');
          dashboardPage.taskList.taskSorting.changeSorting(0, 'Task name');
        });
      });

      beforeEach(function() {
        dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
        editModalPage.selectPanelByKey('criteria');
      });

      it('should add created before criterion', function() {

        // when
        editModalPage.addCriterion('Dates', 'Created Before', '${ now() }');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(3);
      });


      it('should add follow up before criterion', function() {

        // when
        editModalPage.addCriterion('Dates', 'Follow Up Before', '2014-08-25T11:00:02');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 2');
      });


      it('should edit follow up criterion', function() {

        // when
        editModalPage.editCriterion(0, 'Dates', 'Follow Up After', '2014-08-25T11:00:02');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
        expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 1');
      });

    });

  });

  describe('multi tenacy', function() {

      before(function() {
        return testHelper(setupFile.multiTenancySetup, function() {
          dashboardPage.navigateToWebapp('Tasklist');
          dashboardPage.authentication.userLogin('admin', 'admin');
        });
      });

      beforeEach(function() {
        dashboardPage.taskFilters.selectFilter(0);
        dashboardPage.taskFilters.editFilter(0);
        editModalPage.selectPanelByKey('criteria');
      });

      it('should add tenant id criterion and validate result', function() {

        // when
        editModalPage.addCriterion('Task', 'Tenant ID In', 'tenant1');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
      });

      it('should add tenant ids criterion and validate result', function() {

        // when
        editModalPage.editCriterion(0, 'Task', 'Tenant ID In', 'tenant1,tenant2');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(2);
      });

      it('should add without tenant id criterion and validate result', function() {

        // when
        editModalPage.removeCriterionButton(0).click();
        editModalPage.addCriterion('Task', 'Without Tenant ID');
        editModalPage.saveFilter();

        // then
        expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(1);
      });

    });

});
