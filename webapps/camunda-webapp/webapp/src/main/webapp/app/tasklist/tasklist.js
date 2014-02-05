/* global ngDefine: false */

/**
 * @module tasklist
 *
 */

/**
 * @namespace cam.tasklist
 */
(function() {
  'use strict';

  /**
   * @memberof cam
   * @name tasklist
   */
  var core = [
    'module:tasklist.pages:./pages/main',
    'module:tasklist.services:./services/main',
    'module:tasklist.directives:./directives/main'];

  var commons = [
    'module:camunda.common.directives:camunda-common/directives/main',
    'module:camunda.common.extensions:camunda-common/extensions/main',
    'module:camunda.common.services:camunda-common/services/main',
    'module:camunda.common.pages:camunda-common/pages/main' ];

  var dependencies = [ 'jquery', 'angular', 'module:ng', 'module:ngResource', 'module:ngSanitize', 'module:ui.bootstrap:angular-ui' ].concat(commons, core);

  ngDefine('tasklist', dependencies, function(module) {

    var ModuleConfig = [ '$routeProvider', 'UriProvider', function($routeProvider, UriProvider) {

      $routeProvider.otherwise({ redirectTo: '/overview' });

      function getUri(id) {
        var uri = $('base').attr(id);
        if (!id) {
          throw new Error('Uri base for ' + id + ' could not be resolved');
        }

        return uri;
      }

      UriProvider.replace(':appName', 'tasklist');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('adminbase://', getUri('app-root') + "/app/admin/");
      UriProvider.replace('tasklist://', getUri('tasklist-api'));
      UriProvider.replace('admin://', getUri('tasklist-api') + "../admin/");
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/app\/tasklist\/(\w+)\//);
        if (match) {
          return match[1];
        } else {
          throw new Error('no process engine selected');
        }
      }]);
    }];

    module.config(ModuleConfig);

    // CAM-1098
    var ProcessDefinitionsController = [ '$scope', 'EngineApi', function($scope, EngineApi) {
      var queryObject = { latest : true };

      $scope.processDefinitions = EngineApi.getProcessDefinitions().query(queryObject);
    }];

    module.controller('ProcessDefinitionsController', ProcessDefinitionsController);
  });

})(window || this);
