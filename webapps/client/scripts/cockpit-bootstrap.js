(function(document, window, require) {
  'use strict';
  var baseUrl = document.getElementsByTagName('base')[0].getAttribute('app-root') +'/';
  var APP_NAME = 'cam.cockpit';
  baseUrl += 'app/cockpit/';
  var pluginPackages = window.PLUGIN_PACKAGES || [];
    require({
      baseUrl:    baseUrl + 'assets/vendor', // for dojo
      packages:   pluginPackages
    });

  var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];
  var dependencies = [
      'angular',
      'angular-resource',
      'angular-sanitize',
      'angular-ui',
      'ngDefine',
      'jquery-ui/ui/jquery.ui.draggable']
  .concat(pluginDependencies.map(function(plugin) {
      return plugin.requirePackageName;
  }));

    require(dependencies, function(angular) {
      require([
        'camunda-cockpit-ui',
        'domReady!'
      ], function() {
        angular.bootstrap(document, [ APP_NAME ]);
        var html = document.getElementsByTagName('html')[0];

        html.setAttribute('ng-app', APP_NAME);
        if (html.dataset) {
          html.dataset.ngApp = APP_NAME;
        }

        if (top !== window) {
          window.parent.postMessage({ type: 'loadamd' }, '*');
        }
      });
  });

})(document, window || this, require);
