define([
  'angular'
], function(
  angular
) {
  'use strict';
  return [
    'UriProvider',
  function(
    UriProvider
  ) {
    var $baseTag = angular.element('base');

    function getUri(name) {
      var uri = $baseTag.attr(name);
      if (!name) {
        throw new Error('Uri base for ' + name + ' could not be resolved');
      }

      return uri;
    }

    UriProvider.replace(':appName', 'admin');
    UriProvider.replace('app://', getUri('href'));
    UriProvider.replace('adminbase://', getUri('app-root') + '/app/admin/');
    UriProvider.replace('tasklistbase://', getUri('app-root') + '/app/tasklist/');
    UriProvider.replace('cockpitbase://', getUri('app-root') + '/app/cockpit/');
    UriProvider.replace('admin://', getUri('admin-api'));
    UriProvider.replace('plugin://', getUri('admin-api') + 'plugin/');
    UriProvider.replace('engine://', getUri('engine-api'));

    UriProvider.replace(':engine', ['$window', function($window) {
      var uri = $window.location.href;

      var match = uri.match(/\/app\/tasklist\/(\w+)(|\/)/);
      if (match) {
        return match[1];
      } else {
        throw new Error('no process engine selected');
      }
    }]);
  }];
});
