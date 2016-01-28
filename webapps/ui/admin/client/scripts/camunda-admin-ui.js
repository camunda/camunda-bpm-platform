'use strict';

var pagesModule = require('./pages/main'),
    directivesModule = require('./directives/main'),
    filtersModule = require('./filters/main'),
    servicesModule = require('./services/main'),
    resourcesModule = require('./resources/main'),
    camCommonsUi = require('camunda-commons-ui/lib'),
    sdk = require('camunda-bpm-sdk-js/lib/angularjs/index'),
    angular = require('angular'),
    $ = require('jquery');


  var APP_NAME = 'cam.admin';

  module.exports = function(pluginDependencies) {

    var ngDependencies = [
      'ng',
      'ngResource',
      camCommonsUi.name,
      directivesModule.name,
      filtersModule.name,
      pagesModule.name,
      resourcesModule.name,
      servicesModule.name
    ].concat(pluginDependencies.map(function(el){
      return el.ngModuleName;
    }));

    var appNgModule = angular.module(APP_NAME, ngDependencies);

    var ModuleConfig = [
      '$routeProvider',
      'UriProvider',
    function(
      $routeProvider,
      UriProvider
    ) {
      $routeProvider.otherwise({ redirectTo: '/users' });

      function getUri(id) {
        var uri = $('base').attr(id);
        if (!id) {
          throw new Error('Uri base for ' + id + ' could not be resolved');
        }

        return uri;
      }

      UriProvider.replace(':appName', 'admin');
      UriProvider.replace('app://', getUri('href'));
      UriProvider.replace('cockpitbase://', getUri('app-root') + '/app/cockpit/');
      UriProvider.replace('admin://', getUri('admin-api'));
      UriProvider.replace('plugin://', getUri('admin-api') + 'plugin/');
      UriProvider.replace('engine://', getUri('engine-api'));

      UriProvider.replace(':engine', [ '$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/\/app\/admin\/(\w+)(|\/)/);
        if (match) {
          return match[1];
        } else {
          throw new Error('no process engine selected');
        }
      }]);
    }];

    appNgModule.config(ModuleConfig);


    appNgModule.controller('camAdminAppCtrl', [
      '$scope',
      'UserResource',
    function (
      $scope,
      UserResource
    ) {
      function getUserProfile(auth) {
        if (!auth || !auth.name) {
          $scope.userFullName = null;
          return;
        }

        UserResource.profile({
          userId: auth.name
        }).$promise.then(function(info) {
          $scope.userFullName = info.firstName + ' ' + info.lastName;
        });
      }

      $scope.$on('authentication.changed', function (ev, auth) {
        getUserProfile(auth);
      });

      getUserProfile($scope.authentication);
    }]);


    $(document).ready(function () {
      angular.bootstrap(document, [ appNgModule.name ]);
      var html = document.getElementsByTagName('html')[0];

      html.setAttribute('ng-app', appNgModule.name);
      if (html.dataset) {
        html.dataset.ngApp = appNgModule.name;
      }

      if (top !== window) {
        window.parent.postMessage({ type: 'loadamd' }, '*');
      }
    });

      /* live-reload
      // loads livereload client library (without breaking other scripts execution)
      $('body').append('<script src="//' + location.hostname + ':LIVERELOAD_PORT/livereload.js?snipver=1"></script>');
      /* */
  };

  module.exports.exposePackages = function(requirePackages) {
    requirePackages.angular = angular;
    requirePackages.jquery = $;
    requirePackages['camunda-commons-ui'] = camCommonsUi;
    requirePackages['camunda-bpm-sdk-js'] = sdk;
  };
