/* global define: false, describe: false, xdescribe: false, beforeEach: false, afterEach: false, module: false, inject: false, xit: false, it: false, expect: false */
/* jshint unused: false */
define([ 'angular', 'cockpit/filters/main' ], function(angular) {
  'use strict';

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('filters', function() {

    xdescribe('shorten number filter', function() {

      beforeEach(function() {
        angular.module('testmodule', [ 'cockpit.filters' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      it('should return 14', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(14)).toBe(14);
      }));

      it('should return 140', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(140)).toBe(140);
      }));

      it('should return 949', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(949)).toBe(949);
      }));

      it('should return 1k (950 -> 1k)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(949)).toBe(949);
      }));

      it('should return 1k (999 -> 1k)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(999)).toBe('1k');
      }));

      it('should return 1k (1000 -> 1k)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(1000)).toBe('1k');
      }));

      it('should return 1.4K (1399 -> 1.4k)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(1000)).toBe('1k');
      }));

      it('should return 1.4K (1400 -> 1.4k)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(1400)).toBe('1.4k');
      }));

      it('should return 1.4K (cornercase 1449)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(1449)).toBe('1.4k');
      }));

      it('should return 1.4K (cornercase 1450)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(1450)).toBe('1.5k');
      }));

      it('should return 0.9M (cornercase 949,999 -> 0.9M)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(949999)).toBe('0.9M');
      }));


      it('should return 1M (cornercase 950,000 -> 1M)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(950000)).toBe('1M');
      }));

      it('should return 1M (1,000,000 -> 1M)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(1000000)).toBe('1M');
      }));

      it('should return 1M (1,400,000 -> 1.4M)', inject(function($filter) {

        var shortenNumberFilter = $filter('shortenNumber');

        expect(shortenNumberFilter(1400000)).toBe('1.4M');
      }));

    });
  });
});
