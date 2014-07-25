/*
 * */
'use strict';

var systemPage = require('../pages/system');

describe('system page -', function() {

  describe('start test', function () {

    it('should login', function () {

      // when
      systemPage.navigateToWebapp('Admin');
      systemPage.login('demo', 'demo');
      systemPage.selectNavbarItem('System');

      // then
      systemPage.general.isActive();
      systemPage.general.loggedInUser('demo');
      expect(systemPage.pageHeader()).toBe('System Settings');
    });

  });

  describe('system sub pages', function() {

    beforeEach(function() {

      systemPage.navigateToWebapp('Admin');
      systemPage.selectNavbarItem('System');
    });


    it('should validate general page', function() {

      // when
      systemPage.selectSystemNavbarItem('General');

      // then
      systemPage.general.isActive();
      expect(systemPage.general.boxHeader()).toBe('General Settings');
    });


    it('should license key page', function() {

      // when
      systemPage.selectSystemNavbarItem('License Key');

      // then
      systemPage.licenseKey.isActive();
      expect(systemPage.licenseKey.boxHeader()).toBe('License Key');
    });

  });

});