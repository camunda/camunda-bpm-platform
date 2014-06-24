describe('admin dashboard', function() {
  'use strict';

  var utils = require('./adminUtils');

  describe('start test', function () {
    it('should start Admin', function () {
      utils.startWebapp('Admin');
    });
  });

  describe('validate elements', function () {
    it('should validate elements when user is logged off', function () {
      expect(element(by.css('.navbar [sem-show-user-actions]')).isPresent()).toBe(false);
      expect(element(by.css('.navbar [sem-show-applications]')).isPresent()).toBe(true);
    });

    it('should validate elements when user is logged on', function () {
      utils.login('jonny1', 'jonny1', true);

      expect(element(by.css('.navbar [sem-show-user-actions]')).isPresent()).toBe(true);
      expect(element(by.css('.navbar [sem-show-applications]')).isPresent()).toBe(true);
    });
  });

  describe('end test', function () {
    it('should log out', function () {
      utils.logoutWebapp();
    })
  });
});