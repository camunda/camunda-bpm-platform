/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./tasklist-search-setup');

var page = require('../pages/dashboard');


describe('Tasklist Search', function() {

  describe('the tasklist page', function() {

    before(function() {
      return testHelper(setupFile.setup1);
    });

    it('should display all tasks initially', function() {

      // when
      page.navigateToWebapp('Tasklist');
      page.authentication.userLogin('test', 'test');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(3);
      expect(page.taskList.taskSearch.variableTypeDropdown('Process Variable').isPresent()).to.eventually.be.false;
    });

    describe('add a search pill', function() {

      it('should use wrong variable type and find nothing', function() {

        // when
        page.taskList.taskSearch.createSearch('Process Variable', 'testVar', '=', '48');

        //then
        expect(page.taskList.taskList().count()).to.eventually.eql(0);
      });


      it('should change variable type and find one task', function() {

        // when
        page.taskList.taskSearch.changeType(0, 'Task Variable');

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(1);
        expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
      });


      it('should change operator and find two tasks', function() {

        // when
        page.taskList.taskSearch.changeOperator(0, '<=');

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(2);
      });

    });


    describe('add more search pills', function() {

      it('should add Date search and find one task', function() {

        // when
        page.taskList.taskSearch.createSearch('Task Variable', 'testDate', 'before', '2013-11-30T10:03:00');

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(1);
        expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
      });


      it('should change operator and find two task', function() {

        // when
        page.taskList.taskSearch.changeOperator(1, 'after');

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(2);
        expect(page.taskList.taskName(1)).to.eventually.eql('Task 1');
      });


      it('should add String search and find Task 2', function() {

        // when
        page.taskList.taskSearch.createSearch('Task Variable', 'testString', 'like', 'hans');

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(1);
        expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
      });


      it('should keep search pills after page refresh', function(done) {

        // when
        browser.getCurrentUrl().then(function(url) {
          browser.get(url).then(function() {
            browser.sleep(500);

            // then
            expect(page.taskList.taskList().count()).to.eventually.eql(1);
            expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');

            done();
          });
        });

      });


      it('should change String search value and find Task 1', function(done) {

        // when
        page.taskList.taskSearch.changeValue(2, '\'4711\'').then(function() {

          // then
          expect(page.taskList.taskList().count()).to.eventually.eql(1);
          expect(page.taskList.taskName(0)).to.eventually.eql('Task 1');

          done();
        });
      });
    });


    describe('remove search pill', function() {

      it('should remove String search', function() {

        // when
        page.taskList.taskSearch.deleteSearch(2);

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(2);
      });


      it('should remove Integer search', function() {

        // when
        page.taskList.taskSearch.deleteSearch(0);

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(2);
      });


      it('should remove Date search', function() {

        // when
        page.taskList.taskSearch.deleteSearch(0);

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(3);
      });

    });


    describe('search by label', function() {

      it('should find tasks by label', function() {
        // when
        page.taskList.taskSearch.createSearch('Task Variable', 'Test Variable', '=', '42');

        //then
        expect(page.taskList.taskList().count()).to.eventually.eql(1);
        expect(page.taskList.taskName(0)).to.eventually.eql('Task 1');
      });


      it('should remove label search', function() {

        // when
        page.taskList.taskSearch.deleteSearch(0);

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(3);
      });

    });


    describe('search task properties', function() {

      it('should search by name like per default', function() {

        // when
        page.taskList.taskSearch.searchInputField().click();
        page.taskList.taskSearch.searchInputField().sendKeys('1', protractor.Key.ENTER);

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(1);
        expect(page.taskList.taskName(0)).to.eventually.eql('Task 1');
      });


      it('should not find a task if search is changed to equal', function() {

        // when
        page.taskList.taskSearch.changeOperator(0, '=');

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(0);
      });


      it('should find tasks by assignee', function() {

        // when
        page.taskList.taskSearch.deleteSearch(0);
        page.taskList.taskSearch.createSearch('Assignee', 'test', '=');

        // then
        expect(page.taskList.taskList().count()).to.eventually.eql(1);
        expect(page.taskList.taskName(0)).to.eventually.eql('Task 3');
      });


      it('should allow to use expressions', function() {

        // when
        page.taskList.taskSearch.deleteSearch(0);
        page.taskList.taskSearch.createSearch('Assignee', '${ currentUser() }', '=');

        // then
        expect(page.taskList.taskListInfoText()).to.eventually.contain('Failure: Loading the list of tasks finished with failures.');
        /*expect(page.taskList.taskList().count()).to.eventually.eql(1);
        expect(page.taskList.taskName(0)).to.eventually.eql('Task 3');*/
      });

    });
  });

  describe('multi tenancy', function() {

    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {
        page.navigateToWebapp('Tasklist');
        page.authentication.userLogin('admin', 'admin');
        page.taskList.taskSorting.changeSorting(0, 'Task name');
      });
    });

    it('should search by tenant id', function() {

      // when
      page.taskList.taskSearch.createSearch('Tenant ID', 'tenant1');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(1);
      expect(page.taskList.taskName(0)).to.eventually.eql('Task 1');
    });

    it('should search by tenant ids', function() {

      // when
      page.taskList.taskSearch.changeValue(0, 'tenant1,tenant2');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(2);
      expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
      expect(page.taskList.taskName(1)).to.eventually.eql('Task 1');
    });

    it('should search tasks without tenant id', function() {

      // when
      page.taskList.taskSearch.deleteSearch(0);
      page.taskList.taskSearch.createSearch('Without Tenant ID');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(1);
      expect(page.taskList.taskName(0)).to.eventually.eql('Task 3');
    });

  });

});
