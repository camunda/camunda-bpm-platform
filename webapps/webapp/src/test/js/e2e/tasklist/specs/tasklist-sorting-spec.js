/*'use strict';

* 3 process instances
  * 1 variable with 3 values
  * 3 Tasks with
    * 3 different dates
    * 1 variable with 3 values

- by default created desc
- order can be switched
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


    it('should validate order', function(done) {


      // then
      expect(dashboardPage.taskList.taskList().count()).to.eventually.eql(3);
      expect(dashboardPage.taskList.taskName(1)).to.eventually.eql('User Task 1');
      expect(dashboardPage.taskList.taskProcessDefinitionName(2)).to.eventually.eql('User Tasks');
    });


    it('should switch order', function() {

      // when
      // ...add page object for sorting widget

      // then

    });

  });


  describe('add sorting', function() {

  });


  describe('change sorting', function() {

  });


  describe('remove sorting', function() {

  });


});
