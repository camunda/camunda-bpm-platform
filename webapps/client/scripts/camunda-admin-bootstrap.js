define('camunda-admin-bootstrap', [
  './scripts/camunda-admin-ui'
], function (camundaAdminUi) {
  'use strict';

  require.config({
    baseUrl: '../../../lib'
  });

  require(['globalize'], function(globalize) {
    var requirePackages = {};

    camundaAdminUi.exposePackages(requirePackages);
    globalize(require, ['angular', 'camunda-commons-ui', 'camunda-bpm-sdk-js'], requirePackages);

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
        camundaAdminUi(pluginDependencies);
      });
    });

  });

});

require(['camunda-admin-bootstrap'], function(){});
