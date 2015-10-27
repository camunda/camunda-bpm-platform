define('camunda-admin-ui', [
  './pages/main',
  './directives/main',
  './filters/main',
  './services/main',
  './resources/main',
  'camunda-commons-ui',
  'ngDefine'
], function () {
  'use strict';
  var APP_NAME = 'cam.admin';

  var pluginPackages = window.PLUGIN_PACKAGES || [];
  var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];


  require.config({
    packages: pluginPackages
  });


  var dependencies = [].concat(pluginDependencies.map(function(plugin) {
    return plugin.requirePackageName;
  }));



  require(dependencies, function() {

    var ngDependencies = [
      'ng',
      'ngResource',
      require('camunda-commons-ui').name,
      require('./directives/main').name,
      require('./filters/main').name,
      require('./pages/main').name,
      require('./resources/main').name,
      require('./services/main').name
    ].concat(pluginDependencies.map(function(el){
      return el.ngModuleName;
    }));

    var angular = require('angular');
    var $ = require('jquery');
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


    require([
      'domReady!'
    ], function () {
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
    require(['jquery'], function($) {
      $('body').append('<script src="//' + location.hostname + ':LIVERELOAD_PORT/livereload.js?snipver=1"></script>');
    });
    /* */
  });
});

