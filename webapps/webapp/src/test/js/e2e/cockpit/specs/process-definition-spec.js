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
      expect(processPage.diagram.instancesBadgeFor('UserTask_1').getText()).to.eventually.eql('2');
    });

    it('should process clicks in diagram', function() {
      processPage.diagram.selectActivity('UserTask_1');

      expect(processPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.true;
      expect(processPage.filter.activityFilter('User Task 1').isPresent()).to.eventually.be.true;
    });

    it('should process clicks in Filter table', function() {
      processPage.filter.removeFilterButton('User Task 1').click();

      expect(processPage.diagram.isActivitySelected('UserTask_1')).to.eventually.be.false;
    });

  });

});
