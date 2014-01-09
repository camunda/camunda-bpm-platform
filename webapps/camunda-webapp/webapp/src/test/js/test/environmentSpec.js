describe('The testing environment', function() {
  'use strict';
  /* global karma: true */

  it('inititalizes', function() {
    expect(window.__karma__).toBeDefined();
    expect(__karma__).toBeDefined();
  });

  describe('The basic setup', function() {
    xit('uses a configuration module', function() {
      expect(require.defined('require-conf')).toBe(true);
    });

    it('contains jQuery', function() {
      expect(require.defined('jquery')).toBe(true);
    });

    it('contains angular.js', function() {
      expect(require.defined('angular')).toBe(true);
    });

    it('contains angular-mocks.js', function() {
      expect(require.defined('angular-mocks')).toBe(true);
    });
  });
});
