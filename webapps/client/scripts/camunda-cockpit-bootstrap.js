define('camunda-cockpit-bootstrap', [
  './scripts/camunda-cockpit-ui'
], function (camundaCockpitUi) {
  'use strict';

  require.config({
    baseUrl: '../../../lib'
  });

  require(['globalize'], function(globalize) {
    var requirePackages = {};

    camundaCockpitUi.exposePackages(requirePackages);
    globalize(require, ['angular', 'camunda-commons-ui', 'camunda-bpm-sdk-js', 'jquery', 'angular-data-depend'], requirePackages);

    var pluginPackages = window.PLUGIN_PACKAGES || [];
    var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];

    require.config({
      packages: pluginPackages,
      baseUrl: '../',
      paths: {
        ngDefine: '../../lib/ngDefine'
      }
    });

    var dependencies = ['angular', 'ngDefine'].concat(pluginDependencies.map(function(plugin) {
      return plugin.requirePackageName;
    }));

    require(dependencies, function() {
      require([],function() {
        camundaCockpitUi(pluginDependencies);
      });
    });

  });

});

require(['camunda-cockpit-bootstrap'], function(){});
