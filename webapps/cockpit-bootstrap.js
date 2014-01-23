(function(document, window, require) {
  /**
   * A helper module to prepare the cockpit plugin for bootstrap.
   * @module cockpit-bootstrap
   */

  var baseUrl = '../../../';
  var APP_NAME = 'cockpit';
  var pluginPackages = window.PLUGIN_PACKAGES || [];

  require([baseUrl +'require-conf'], function(rjsConf) {
    require({
      baseUrl:    baseUrl,
      paths:      rjsConf.paths,
      shim:       rjsConf.shim,
      packages:   rjsConf.packages.concat(pluginPackages)
    });

    require([
      'angular',
      'angular-resource',
      'angular-sanitize',
      'angular-ui',
      'ngDefine',
      'bootstrap',
      'jquery-ui'
    ], function(angular) {
      require([
        APP_NAME,
        'domReady!'
      ], function() {
        rjsConf.utils.bootAngular(angular, APP_NAME);
      });
    });
  });


})(document, window || this, require);
