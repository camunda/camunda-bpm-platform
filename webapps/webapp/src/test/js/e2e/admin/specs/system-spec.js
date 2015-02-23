'use strict';

var testHelper = require('../../test-helper');

var systemPage = require('../pages/system');

describe('Admin system Spec', function() {

  before(function(done) {
    testHelper(done);

    systemPage.navigateToWebapp('Admin');
    systemPage.authentication.userLogin('admin', 'admin');
  });


  describe('navigate to system pages', function() {

    it('should navigate to system menu', function () {

      // when
      systemPage.selectNavbarItem('System');

      // then
      systemPage.general.isActive();
      systemPage.general.loggedInUser('admin');
      expect(systemPage.pageHeader()).to.eventually.eql('System Settings');
    });


    it('should validate general page', function() {

      // when
      systemPage.selectSystemNavbarItem('General');

      // then
      systemPage.general.isActive();
      expect(systemPage.general.boxHeader()).to.eventually.eql('General Settings');
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
