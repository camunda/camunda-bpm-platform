'use strict';


define('camunda-tasklist-ui', [
  'camunda-tasklist-ui/require-conf',
  'camunda-tasklist-ui/utils',
], function(
  rjsConf
) {
  /**
   * @namespace cam
   */

  /**
   * @module cam.tasklist
   */

  var tasklistApp;


  var appModules = rjsConf.shim['camunda-tasklist-ui'];


  var deps = [
    'angular',
    'text!camunda-tasklist-ui/index.html'
  ].concat(appModules);



  // converts AMD paths to angular module names
  // "camunda-tasklist-ui/pile" will be "cam.tasklist.pile"
  function rj2ngNames(names) {
    var name, translated = [];
    for (var n = 0; n < names.length; n++) {
      name = (require(names[n]) || {}).name;
      if (name) translated.push(name);
    }
    return translated;
  }



  function loaded() {
    var angular = require('angular');
    var $ = angular.element;

    var ngDeps = rj2ngNames(appModules).concat([
      'pascalprecht.translate',
      'ngRoute'
    ]);

    tasklistApp = angular.module('cam.tasklist', ngDeps);

    tasklistApp.config([
      'UriProvider',
    function(
      UriProvider
    ) {
      var $baseTag = $('base');

      function getUri(name) {
        var uri = $baseTag.attr(name);
        if (!name) {
          throw new Error('Uri base for ' + name + ' could not be resolved');
        }

        return uri;
      }

      UriProvider.replace(':appName', 'tasklist');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('adminbase://', getUri('app-root') + '/app/admin/');
      UriProvider.replace('tasklistbase://', getUri('app-root') + '/app/tasklist/');
      UriProvider.replace('cockpitbase://', getUri('app-root') + '/app/cockpit/');
      UriProvider.replace('admin://', getUri('admin-api'));
      UriProvider.replace('plugin://', getUri('admin-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      // for forms
      UriProvider.replace('app:', 'tasklist');
      UriProvider.replace('embedded:', getUri('app-root'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/\/app\/tasklist\/(\w+)(|\/)/);
        if (match) {
          return match[1];
        } else {
          throw new Error('no process engine selected');
        }
      }]);
    }]);

    tasklistApp.config([
      '$routeProvider',
      '$locationProvider',
      '$translateProvider',
    function(
      $routeProvider,
      $locationProvider,
      $translateProvider
    ) {

      // Simply register translation table as object hash
      $translateProvider
        .translations('en', require('json!locales/en.json'))
        .translations('de', require('json!locales/de.json'))
        .translations('fr', require('json!locales/fr.json'))

        // using the determinePreferredLanguage()
        // would lead to use something like "en_US"
        .determinePreferredLanguage()
        .fallbackLanguage('en')
      ;

      var tasklistTemplate = require('text!camunda-tasklist-ui/index.html');

      $routeProvider
        .when('/', {
          template: tasklistTemplate,
          authentication: 'required'
        })

        // // Would be great to be able to start processes with a URL
        // .when('/process/:processDefinitionId/start', {
        //   template: tasklistTemplate,
        //   controller: 'processStartCtrl'
        // })
        // .when('/process/key/:processDefinitionKey/start', {
        //   template: tasklistTemplate,
        //   controller: 'processStartCtrl'
        // })


        .when('/login', {
          template: tasklistTemplate,
          controller: 'userLoginCtrl'
        })


        .when('/logout', {
          template: tasklistTemplate,
          controller: 'userLogoutCtrl'
        })


        .otherwise({
          redirectTo: '/'
        })
      ;
    }]);

    var notificationsPanel = require('camunda-commons-ui/directives/notificationsPanel');
    tasklistApp.directive('notificationsPanel', notificationsPanel);

    tasklistApp.config(require('camunda-tasklist-ui/config/uris'));
    tasklistApp.config(require('camunda-tasklist-ui/config/translations'));
    tasklistApp.config(require('camunda-tasklist-ui/config/routes'));

    $(document).ready(function() {
      angular.bootstrap(document, ['cam.tasklist', 'cam.embedded.forms']);
    });
  }


  // configure require.js
  require.config(rjsConf);

  // and load the dependencies
  require(deps, loaded);
});
