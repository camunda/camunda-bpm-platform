'use strict';

/*
* 3 process instances
  * 1 variable with 3 values
  * 3 Tasks with
    * 3 different dates
    * 1 variable with 3 values

- by default created desc +
- order can be switched +
- can add sorting
  - when not variable
    - dropdown opens
    - click on a "scope"
    - dropdown closes
    - URL get updated
    - sorting is update
  - when variable
    - dropdown opens
    - when click on a "variable scope"
      - inputs are visible
      - button has "add"
      - when click button
        - dropdown closes
        - URL get updated
        - sorting is updated
- can change "scope"
  - when not "scope variable"
    - dropdown opens
    - when click on "scope"
      - dropdown closes
      - URL get updated
      - sorting is updated
    - when click on "variable scope"
      - inputs are visible
      - button has "change"
      - when click button
        - dropdown closes
        - URL get updated
        - sorting is updated
  - when "scope variable"
    - dropdown opens
    - when click on "scope"
      - dropdown closes
      - URL get updated
      - sorting is updated
    - when click on "variable scope"
      - inputs are visible
      - button has "change"
      - when click button
        - dropdown closes
        - URL get updated
        - sorting is updated
- can remove sorting
  - URL get updated
  - sorting is updated
*/

var testHelper = require('../../test-helper');
var setupFile = require('./tasklist-sorting-setup');

var dashboardPage = require('../pages/dashboard');

describe('Tasklist Sorting Spec', function() {

  describe('sorting by default', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });


    it('should validate sorting choice', function() {

      // when

      // then
      expect(dashboardPage.taskList.taskSorting.addSortingButton().isPresent()).to.eventually.be.true;
      expect(dashboardPage.taskList.taskSorting.removeSortingButton(0).isPresent()).to.eventually.be.false;
      expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(1);
      expect(dashboardPage.taskList.taskSorting.sortingName(0).getText()).to.eventually.eql('created');
      expect(dashboardPage.taskList.taskSorting.isSortingDescending(0)).to.eventually.be.true;
    });


    it('should switch order', function() {

      // when
      dashboardPage.taskList.taskSorting.changeSortingDirection(0);

      // then
      dashboardPage.taskList.taskSorting.isSortingAscending(0);
    });


    describe('add sorting', function() {

      it('should add "Task name" sorting', function() {

        // when
        dashboardPage.taskList.taskSorting.addNewSorting('Task name');

        // then
        expect(dashboardPage.taskList.taskSorting.sortingName(1).getText()).to.eventually.eql('task name');
        expect(dashboardPage.taskList.taskSorting.isSortingDescending(1)).to.eventually.be.true;
        expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(2);
      });


      it('should add "Priority" sorting', function() {

        // when
        dashboardPage.taskList.taskSorting.addNewSorting('Priority');

        // then
        expect(dashboardPage.taskList.taskSorting.sortingName(2).getText()).to.eventually.eql('priority');
        expect(dashboardPage.taskList.taskSorting.isSortingDescending(2)).to.eventually.be.true;
        expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(3);
      });


      it('should add "Follow-Up Date" sorting', function() {

        // when
        dashboardPage.taskList.taskSorting.addNewSorting('Follow-up date');

        // then
        expect(dashboardPage.taskList.taskSorting.sortingName(3).getText()).to.eventually.eql('follow-up date');
        expect(dashboardPage.taskList.taskSorting.isSortingDescending(3)).to.eventually.be.true;
        expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(4);
      });


      it('should add "Process Variable" sorting', function() {

        // when
        dashboardPage.taskList.taskSorting.addNewSorting('Process Variable', 'myStringVariable', 'String');

        // then
        expect(dashboardPage.taskList.taskSorting.sortingName(4).getText()).to.eventually.eql('myStringVariable');
        expect(dashboardPage.taskList.taskSorting.isSortingDescending(4)).to.eventually.be.true;
        expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(5);
      });


      it('should validate sorting selection dropdown', function() {

        // when
        dashboardPage.taskList.taskSorting.addSortingButton().click();

        // then
        expect(dashboardPage.taskList.taskSorting.newSortingSelectionListElement('Task name').isPresent()).to.eventually.be.false;
        expect(dashboardPage.taskList.taskSorting.newSortingSelectionListElement('Priority').isPresent()).to.eventually.be.false;
        expect(dashboardPage.taskList.taskSorting.newSortingSelectionListElement('Follow-up date').isPresent()).to.eventually.be.false;
        expect(dashboardPage.taskList.taskSorting.newSortingSelectionListElement('Process Variable').isPresent()).to.eventually.be.true;
      });

    });

  });


  describe('change sorting', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });


    it('should change sorting type to "Assignee"', function() {

      // when
      dashboardPage.taskList.taskSorting.changeSorting(0, 'Assignee');

      // then
      expect(dashboardPage.taskList.taskSorting.sortingName(0).getText()).to.eventually.eql('assignee');
      expect(dashboardPage.taskList.taskSorting.isSortingDescending(0)).to.eventually.be.true;
      expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(1);
    });


    it('should add "Due date" sorting', function() {

      // when
      dashboardPage.taskList.taskSorting.addNewSorting('Due date');

      // then
      expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 2');
      expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('My Task');
      expect(dashboardPage.taskList.taskName(2)).to.eventually.eql('Task 1');
    });


    it('should switch order', function() {

      // when
      dashboardPage.taskList.taskSorting.changeSortingDirection(1);

      // then
      dashboardPage.taskList.taskSorting.isSortingAscending(1);
      expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 1');
      expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('My Task');
      expect(dashboardPage.taskList.taskName(2)).to.eventually.eql('Task 2');
    });


    it('should change sorting type to "Task Name"', function() {

      // when
      dashboardPage.taskList.taskSorting.changeSorting(1, 'Task name');

      // then
      expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('My Task');
      expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('Task 1');
      expect(dashboardPage.taskList.taskName(2)).to.eventually.eql('Task 2');
    });


    it('should switch order', function() {

      // when
      dashboardPage.taskList.taskSorting.changeSortingDirection(1);

      // then
      dashboardPage.taskList.taskSorting.isSortingDescending(1);
      expect(dashboardPage.taskList.taskName(0)).to.eventually.eql('Task 2');
      expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('Task 1');
      expect(dashboardPage.taskList.taskName(2)).to.eventually.eql('My Task');
    });

  });


  describe('remove sorting', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');
      });
    });


    it('should add a two sortings', function() {

      // when
      dashboardPage.taskList.taskSorting.addNewSorting('Follow-up date');
      dashboardPage.taskList.taskSorting.addNewSorting('Priority');

      // then
      expect(dashboardPage.taskList.taskSorting.sortingName(0).getText()).to.eventually.eql('created');
      expect(dashboardPage.taskList.taskSorting.sortingName(1).getText()).to.eventually.eql('follow-up date');
      expect(dashboardPage.taskList.taskSorting.sortingName(2).getText()).to.eventually.eql('priority');
      expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(3);
    });


    it('should delete "Follow-up date" sorting', function() {

      // when
      dashboardPage.taskList.taskSorting.removeSortingButton(1).click();

      // then
      expect(dashboardPage.taskList.taskSorting.sortingName(0).getText()).to.eventually.eql('created');
      expect(dashboardPage.taskList.taskSorting.sortingName(1).getText()).to.eventually.eql('priority');
      expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(2);
    });


    it('should delete "Created" sorting', function() {

      // when
      dashboardPage.taskList.taskSorting.removeSortingButton(0).click();

      // then
      expect(dashboardPage.taskList.taskSorting.sortingName(0).getText()).to.eventually.eql('priority');
      expect(dashboardPage.taskList.taskSorting.sortingList().count()).to.eventually.eql(1);
    });


    it('should check remove button', function() {

      // then
      expect(dashboardPage.taskList.taskSorting.removeSortingButton(0).isPresent()).to.eventually.be.false;
    });

  });


});
