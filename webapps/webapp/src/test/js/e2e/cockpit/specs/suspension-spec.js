'use strict';

var fs = require('fs');

var testHelper = require('../../test-helper');
var setupFile = require('./suspension-setup');

var dashboardPage = require('../pages/dashboard');
var definitionPage = require('../pages/process-definition');
var instancePage = require('../pages/process-instance');

describe('Cockpit Suspsension Spec', function() {

  describe.skip('process definition suspension', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });

  });


  describe('process instance suspension', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
        definitionPage.processInstancesTab.instanceIdClick(0);
      });
    });


    it('should suspend instance', function() {

      // when
      instancePage.suspension.suspendInstance();

      // then
      expect(instancePage.isInstanceSuspended()).to.eventually.be.true;
    });


    it('should validate suspended instance', function() {

      // when
      instancePage.selectBreadCrumb(1);

      // then
      expect(definitionPage.processInstancesTab.isInstanceSuspended(0)).to.eventually.be.true;

      // finaly
      definitionPage.processInstancesTab.instanceIdClick(0);
    });


    it('should active instance', function() {

      // when
      instancePage.suspension.activateInstance();

      // then
      expect(instancePage.isInstanceSuspended()).to.eventually.be.false;
    });

  });


  describe.skip('job suspension', function() {

    before(function() {
      return testHelper(setupFile, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.deployedProcessesList.selectProcess(0);
      });
    });

  });

});
