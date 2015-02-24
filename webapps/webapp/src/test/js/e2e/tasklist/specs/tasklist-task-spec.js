/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./tasklist-task-setup');

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
    expect(page.taskList.taskList().count()).to.eventually.eql(2);
  });

  it('should display only tasks which match a search', function() {
    // when
    page.taskList.taskSearch.createSearch('Task Variable', 'testVar', '=', '42');

    //then
    expect(page.taskList.taskList().count()).to.eventually.eql(1);
    expect(page.taskList.taskName(0)).to.eventually.eql('Task 1');
  });

  it('should reset the tasklist when the search is deleted', function() {
    // when
    page.taskList.taskSearch.deleteSearch(0);

    // then
    expect(page.taskList.taskList().count()).to.eventually.eql(2);
  });

});
