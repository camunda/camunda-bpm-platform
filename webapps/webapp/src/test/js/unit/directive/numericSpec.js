/* global define: false, describe: false, xdescribe: false, beforeEach: false, afterEach: false, module: false, inject: false, xit: false, it: false, expect: false */
/* jshint unused: false */
define([
  'angular',
  'jquery',
  'cockpit/directives/numeric'
], function(angular, $) {
  'use strict';

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {
    /* global browserTrigger: false */
    xdescribe('numeric directive', function() {
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
        angular.module('testmodule', [ 'cockpit.directives' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      it('should accept integer value', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3;

        element = $compile('<input numeric integer="true" ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(false);
        expect(element.hasClass('ng-valid')).toBe(true);
        expect($rootScope.value).toBe(7);
      }));

      it('should mark integer input as invalid (1)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3;

        element = $compile('<input numeric integer="true" ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7a');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7);
      }));

      it('should mark integer input as invalid (2)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3;

        element = $compile('<input numeric integer="true" ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('a7');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect(isNaN($rootScope.value)).toBe(true);
      }));

      it('should mark integer input as invalid (3)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3;

        element = $compile('<input numeric integer="true" ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7asdf8');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7);
      }));

      it('should accept float value (1)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7.123');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(false);
        expect(element.hasClass('ng-valid')).toBe(true);
        expect($rootScope.value).toBe(7.123);
      }));

      it('should accept float value (2)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('1.7976931348623157e+308');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(false);
        expect(element.hasClass('ng-valid')).toBe(true);
        expect($rootScope.value).toBe(1.7976931348623157e+308);
      }));

      it('should mark float input as invalid (1)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7.123a');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7.123);
      }));

      it('should mark float input as invalid (2)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('a7.123');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect(isNaN($rootScope.value)).toBe(true);
      }));

      it('should mark float input as invalid (3)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7a.123');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7);
      }));

      it('should mark float input as invalid (4)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7.1a23');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7.1);
      }));

      it('should mark float input as invalid (5)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7.1+23');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7.1);
      }));

      it('should mark float input as invalid (6)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7.1-23');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7.1);
      }));

      it('should mark float input as invalid (7)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = 3.5;

        element = $compile('<input numeric ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('7a7.123');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-numeric')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe(7);
      }));

    });
  });
});
