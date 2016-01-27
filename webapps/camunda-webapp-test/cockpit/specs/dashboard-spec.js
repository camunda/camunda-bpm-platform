'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./dashboard-setup');

var dashboardPage = require('../pages/dashboard');


describe('Cockpit Dashboard Spec', function() {

  describe('dashboard page navigation', function() {

    before(function() {
      return testHelper(setupFile.setup1, function() {

        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });
    });

    it('should validate processes plugin', function() {

      // then
      dashboardPage.isActive();
      expect(dashboardPage.deployedProcessesList.processCountHeader()).to.eventually.eql('1 process definition deployed');
    });

    it('shows an active link in the header', function () {
      expect(dashboardPage.navbarItem(0).getAttribute('class')).to.eventually.contain('active');
    });

    it('should validate process previews tab', function() {

      // when
      dashboardPage.deployedProcessesPreviews.switchTab();

      // then
      expect(dashboardPage.deployedProcessesPreviews.processesPreviews().count()).to.eventually.eql(1);
    });

    it('should validate prosess list tab', function() {

      // when
      dashboardPage.deployedProcessesList.switchTab();

      // then
      expect(dashboardPage.deployedProcessesList.processesList().count()).to.eventually.eql(1);
      expect(dashboardPage.deployedProcessesList.processName(0)).to.eventually.eql('Failing Process');
      expect(dashboardPage.deployedProcessesList.runningInstances(0)).to.eventually.eql('0');
    });

    it('should not display report column in process list tab', function() {
      // then
      expect(dashboardPage.deployedProcessesList.getReportColumn().isPresent()).to.eventually.be.false;
    });


    describe('start instance and validate', function() {

      before(function() {
        return testHelper(setupFile.setup2, true);
      });

      it('should count number of processes', function() {

        // when
        dashboardPage.navigateTo();

        // then
        expect(dashboardPage.deployedProcessesList.processCountHeader()).to.eventually.eql('1 process definition deployed');
        expect(dashboardPage.deployedProcessesList.processesList().count()).to.eventually.eql(1);
        expect(dashboardPage.deployedProcessesList.runningInstances(0)).to.eventually.eql('1');
      });

    });


    describe('deploy process and validate', function() {

      before(function() {
        return testHelper(setupFile.setup3, true);
      });

      it('should validate process list', function() {

        // when
        dashboardPage.navigateTo();

        // then
        expect(dashboardPage.deployedProcessesList.processCountHeader()).to.eventually.eql('2 process definitions deployed');
        expect(dashboardPage.deployedProcessesList.processesList().count()).to.eventually.eql(2);
        expect(dashboardPage.deployedProcessesList.runningInstances(1)).to.eventually.eql('1');
        expect(dashboardPage.deployedProcessesList.processName(1)).to.eventually.eql('processWithSubProcess')
      });


      it('should validate process previews', function() {

        // when
        dashboardPage.deployedProcessesPreviews.switchTab();

        // then
        expect(dashboardPage.deployedProcessesPreviews.processesPreviews().count()).to.eventually.eql(2);
      });

    });

    describe('deploy decision and validate', function() {

      before(function() {
        return testHelper(setupFile.setup4, true);
      });

      it('should show deployed decision', function() {

        // when
        dashboardPage.navigateTo();

        // then
        expect(dashboardPage.deployedDecisionsList.decisionCountHeader()).to.eventually.eql('1 decision table deployed');
        expect(dashboardPage.deployedDecisionsList.decisionsList().count()).to.eventually.eql(1);
        expect(dashboardPage.deployedDecisionsList.decisionName(0)).to.eventually.eql('Assign Approver');
      });

    });

    describe('deploy decision without name', function() {

      before(function() {
        return testHelper(setupFile.setup5, true);
      });

      it('should show decision key', function() {

        // when
        dashboardPage.navigateTo();

        // then
        expect(dashboardPage.deployedDecisionsList.decisionCountHeader()).to.eventually.eql('2 decision tables deployed');
        expect(dashboardPage.deployedDecisionsList.decisionsList().count()).to.eventually.eql(2);
        expect(dashboardPage.deployedDecisionsList.decisionName(0)).to.eventually.eql('invoice-approver');
      });

    });

  });

});
