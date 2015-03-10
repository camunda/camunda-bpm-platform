/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./tasklist-search-setup');

var page = require('../pages/dashboard');

describe('Tasklist Search', function() {

  before(function() {
    return testHelper(setupFile);
  });


  it('should display all tasks initially', function() {

    // when
    page.navigateToWebapp('Tasklist');
    page.authentication.userLogin('test', 'test');

    // then
    expect(page.taskList.taskList().count()).to.eventually.eql(3);
  });


  describe('add a search pill', function() {

    it('should use wrong variable type and find nothing', function(done) {

      // when
      page.taskList.taskSearch.createSearch('Process Variable', 'testVar', '=', '48');

      //then
      expect(page.taskList.taskList().count()).to.eventually.eql(0);
    });


    it('should change variable type and find one task', function(done) {

      // when
      page.taskList.taskSearch.changeType(0, 'Task Variable');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(1);
      expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
    });


    it('should change operator and find two tasks', function(done) {

      // when
      page.taskList.taskSearch.changeOperator(0, '<=');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(2);
    });

  });


  describe('add more search pills', function() {

    it('should add Date search and find one task', function(done) {

      // when
      page.taskList.taskSearch.createSearch('Task Variable', 'testDate', 'before', '2013-11-30T10:03:00');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(1);
      expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
    });


    it('should change operator and find two task', function(done) {

      // when
      page.taskList.taskSearch.changeOperator(1, 'after');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(2);
      expect(page.taskList.taskName(1)).to.eventually.eql('Task 1');
    });


    it('should add String search and find one task', function(done) {

      // when
      page.taskList.taskSearch.createSearch('Task Variable', 'testString', 'like', 'hans');

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(1);
      expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
    });


    it('should keep search pills after page refresh', function(done) {

      // when
      browser.refresh();

      // then
      expect(page.taskList.taskList().count()).to.eventually.eql(1);
      expect(page.taskList.taskName(0)).to.eventually.eql('Task 2');
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
});
