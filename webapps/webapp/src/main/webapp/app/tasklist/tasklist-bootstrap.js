(function(document, window, require) {
  'use strict';

  var baseUrl = document.getElementsByTagName('base')[0].getAttribute('app-root') +'/';
  var APP_NAME = 'tasklist';
  var pluginPackages = window.PLUGIN_PACKAGES || [];

  require([baseUrl +'require-conf.js'], function(rjsConf) {
    require({
      baseUrl:    baseUrl,
      urlArgs:    rjsConf.urlArgs,
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
