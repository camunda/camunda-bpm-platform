describe('Admin group settings', function() {
  'use strict';

  var utils = require('./adminUtils');

  describe('start test', function () {
    it('should start Admin and login', function () {
      utils.startWebapp('Admin');
      utils.login('jonny1', 'jonny1', true);
    });
  });

  describe('end test', function () {
    it('should log out', function () {
      utils.logoutWebapp();
    })
  });
});