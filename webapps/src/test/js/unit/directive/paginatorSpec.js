/* global define: false, describe: false, xdescribe: false, beforeEach: false, afterEach: false, module: false, inject: false, xit: false, it: false, expect: false */
/* jshint unused: false */
define([
  'angular',
  'jquery',
  'camunda-common/directives/main',
  'camunda-common/extensions/main'
], function(angular, $) {
  'use strict';

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    xdescribe('paginator directive', function() {
      var element;

      function createElement(content) {
        return $(content).appendTo(document.body);
      }

      afterEach(function() {
        $(document.body).html('');
        /* global dealoc: false */
        dealoc(element);
      });

      beforeEach(function() {
        angular.module('testmodule', [ 'camunda.common.directives', 'camunda.common.extensions' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      it('creates full pagination <= 10 elements', inject(function($rootScope, $compile) {

        $rootScope.totalPages = 10;
        $rootScope.currentPage = 3;

        element = createElement('<paginator total-pages="totalPages" current-page="currentPage" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var text = element.text();
        text = text.replace(/\s+/gm, ' ');

        expect(text).toEqual(' « 1 2 3 4 5 6 7 8 9 10 » ');
      }));

      it('creates compact pagination > 10 elements', inject(function($rootScope, $compile) {

        $rootScope.totalPages = 12;
        $rootScope.currentPage = 3;

        element = createElement('<paginator total-pages="totalPages" current-page="currentPage" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var text = element.text();
        text = text.replace(/\s+/gm, ' ');

        expect(text).toEqual(' « 1 2 3 4 … 12 » ');
      }));

      it('creates compact pagination > 10 elements (2)', inject(function($rootScope, $compile) {

        $rootScope.totalPages = 12;
        $rootScope.currentPage = 11;

        element = createElement('<paginator total-pages="totalPages" current-page="currentPage" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var text = element.text();
        text = text.replace(/\s+/gm, ' ');

        expect(text).toEqual(' « 1 … 10 11 12 » ');
      }));

      it('creates compact pagination > 10 elements (3)', inject(function($rootScope, $compile) {

          $rootScope.totalPages = 12;
          $rootScope.currentPage = 6;

          element = createElement('<paginator total-pages="totalPages" current-page="currentPage" />');
          element = $compile(element)($rootScope);

          $rootScope.$digest();

          var text = element.text();
          text = text.replace(/\s+/gm, ' ');

          expect(text).toEqual(' « 1 … 5 6 7 … 12 » ');
      }));

    });
  });
});
