/* jshint ignore:start */
'use strict';

var fs = require('fs');

var testHelper = require('../../test-helper');
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var processPage = require('../pages/process-definition');

describe('Cockpit Process Definition Spec', function() {

  describe('diagram interaction', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });


    it('should display process diagram', function() {
      expect(processPage.diagram.diagramElement().isDisplayed()).to.eventually.be.true;
    });


    it('should display the number of running process instances', function() {
      expect(processPage.diagram.instancesBadgeFor('UserTask_1').getText()).to.eventually.eql('3');
    });


    it('should select activity', function() {

      // when
      processPage.diagram.selectActivity('UserTask_1');

      // then
      expect(processPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
      expect(processPage.filter.activitySelection('User Task 1').isPresent()).to.eventually.be.true;
    });


    it('should keep selection after page refresh', function() {

      // when
      browser.getCurrentUrl().then(function (url) {
        browser.get(url);
      });

      // then
      expect(processPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
    });


    it('should process clicks in Filter table', function() {

      // when
      processPage.filter.removeSelectionButton('User Task 1').click();

      // then
      expect(processPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
    });

  });

});
