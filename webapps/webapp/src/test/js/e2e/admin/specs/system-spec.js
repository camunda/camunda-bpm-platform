'use strict';

var testHelper = require('../../test-helper');

var systemPage = require('../pages/system');

var setupFile = require('./system-setup');


describe('Admin system Spec', function() {

  before(function() {
    return testHelper(setupFile.setup1, function() {

      systemPage.navigateToWebapp('Admin');
      systemPage.authentication.userLogin('admin', 'admin');
    });
  });


  describe('navigate to system pages', function() {

    it('should navigate to system menu', function() {

      // when
      systemPage.selectNavbarItem('System');

      // then
      systemPage.general.isActive();
      systemPage.general.loggedInUser('admin');
      expect(systemPage.pageHeader()).to.eventually.eql('System Settings'.toUpperCase());
    });


    it('should validate general page', function() {

      // when
      systemPage.selectSystemNavbarItem('General');

      // then
      systemPage.general.isActive();
      expect(systemPage.general.boxHeader()).to.eventually.eql('General Settings');
    });


    it('should validate flow node count page', function() {

      // when
      systemPage.selectSystemNavbarItem('Flow Node Count');

      // then
      systemPage.flowNodeCount.isActive();
      expect(systemPage.flowNodeCount.boxHeader()).to.eventually.eql('Flow Node Count');
      expect(systemPage.flowNodeCount.resultField()).to.eventually.eql('3');
    });


    it('should support time range', function() {

      // given
      // we are on the flow node count page

      // when
      systemPage.flowNodeCount.startDateField().clear();
      systemPage.flowNodeCount.endDateField().clear();

      systemPage.flowNodeCount.startDateField('2014-01-01T00:00:00');
      systemPage.flowNodeCount.endDateField('2014-12-31T23:59:59');
      systemPage.flowNodeCount.refreshButton().click();

      // then
      expect(systemPage.flowNodeCount.resultField()).to.eventually.eql('0');
    });


    xit('should validate license key page', function() {

      // when
      systemPage.selectSystemNavbarItem('License Key');

      // then
      systemPage.licenseKey.isActive();
      expect(systemPage.licenseKey.boxHeader()).to.eventually.eql('License Key');
    });

  });

});
