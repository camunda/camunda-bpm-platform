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

    xdescribe('state circle directive', function() {
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

      it('should create a green circle', inject(function($rootScope, $compile) {

        // $rootScope.incidents;

        element = createElement('<state-circle incidents="incidents" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-green')).toBe(true);

        expect(element.hasClass('circle-red')).toBe(false);
      }));

      it('should create a green circle (1)', inject(function($rootScope, $compile) {

        // $rootScope.incidents;
        $rootScope.incidentTypes = ['failedJob'];

        element = createElement('<state-circle incidents="incidents" incidents-for-type="incidentType"/>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-green')).toBe(true);

        expect(element.hasClass('circle-red')).toBe(false);
      }));

      it('should create a green circle (2)', inject(function($rootScope, $compile) {

        // $rootScope.incidents;
        $rootScope.incidentTypes = ['failedJob'];

        element = createElement('<state-circle incidents="incidents" incidents-for-type="[\'failedJob\']"/>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-green')).toBe(true);

        expect(element.hasClass('circle-red')).toBe(false);
      }));

      it('should create a green circle when an incident for a specific type has not been found (1)', inject(function($rootScope, $compile) {

        $rootScope.incidents =[{ incidentType: 'anIncident', incidentCount: 1 }];
        $rootScope.incidentsType = ['failedJob'];

        element = createElement('<state-circle incidents="incidents" incidents-for-types="incidentsType" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-green')).toBe(true);

        expect(element.hasClass('circle-red')).toBe(false);
      }));

      it('should create a green circle when an incident for a specific type has not been found (1)', inject(function($rootScope, $compile) {

        $rootScope.incidents =[{ incidentType: 'anIncident', incidentCount: 1 }];

        element = createElement('<state-circle incidents="incidents" incidents-for-types="[\'failedJob\']" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-green')).toBe(true);

        expect(element.hasClass('circle-red')).toBe(false);
      }));

      it('should create a red circle when an incident was found but without any specific incident type', inject(function($rootScope, $compile) {

        $rootScope.incidents =[{ incidentType: 'anIncident', incidentCount: 1 }];

        element = createElement('<state-circle incidents="incidents" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-red')).toBe(true);

        expect(element.hasClass('circle-green')).toBe(false);
      }));

      it('should create a red circle when an incident to a specific incident type was found (1)', inject(function($rootScope, $compile) {

        $rootScope.incidents =[{ incidentType: 'anIncident', incidentCount: 1 }];

        element = createElement('<state-circle incidents="incidents" incidents-for-types="[\'anIncident\']" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-red')).toBe(true);

        expect(element.hasClass('circle-green')).toBe(false);
      }));

      it('should create a red circle when an incident to a specific incident type was found (2)', inject(function($rootScope, $compile) {

        $rootScope.incidents =[{ incidentType: 'anIncident', incidentCount: 1 }];
        $rootScope.incidentsType = ['anIncident'];

        element = createElement('<state-circle incidents="incidents" incidents-for-types="incidentsType" />');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.hasClass('circle')).toBe(true);
        expect(element.hasClass('circle-red')).toBe(true);

        expect(element.hasClass('circle-green')).toBe(false);
      }));

    });
  });
});
