'use strict';

module.exports = function(ngModule, config) {

  ngModule.config([
    'UriProvider',
    function(
        UriProvider
      ) {

      UriProvider.replace(':appName', 'tasklist');
      UriProvider.replace('app://', config.href);
      UriProvider.replace('adminbase://', config['app-root'] + '/app/admin/');
      UriProvider.replace('tasklistbase://', config['app-root'] + '/app/tasklist/');
      UriProvider.replace('cockpitbase://', config['app-root'] + '/app/cockpit/');
      UriProvider.replace('admin://', config['admin-api']);
      UriProvider.replace('plugin://', config['tasklist-api'] + 'plugin/');
      UriProvider.replace('engine://', config['engine-api']);

      UriProvider.replace(':engine', ['$window', function($window) {
        var uri = $window.location.href;

        var match = uri.match(/\/app\/tasklist\/([\w-]+)(|\/)/);
        if (match) {
          return match[1];
        } else {
          throw new Error('no process engine selected');
        }
      }]);
    }]);
};
