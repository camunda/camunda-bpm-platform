__define('camunda-admin-bootstrap', [
  './scripts/camunda-admin-ui'
], function () {
  'use strict';

  var camundaAdminUi = window.CamundaAdminUi;

  requirejs.config({
    baseUrl: '../../../lib'
  });

  var requirePackages = window;
  camundaAdminUi.exposePackages(requirePackages);

  window.define = window.__define;
  window.require = window.__require;

  requirejs(['globalize'], function(globalize) {
    globalize(requirejs, ['angular', 'camunda-commons-ui', 'camunda-bpm-sdk-js', 'jquery'], requirePackages);

    var pluginPackages = window.PLUGIN_PACKAGES || [];
    var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];

    pluginPackages.forEach(function(plugin) {
      var node = document.createElement('link');
      node.setAttribute('rel', 'stylesheet');
      node.setAttribute('href', plugin.location + '/plugin.css');
      document.head.appendChild(node);
    });

    requirejs.config({
      packages: pluginPackages,
      baseUrl: '../',
      paths: {
        ngDefine: '../../lib/ngDefine'
      }
    });

    var dependencies = ['angular', 'ngDefine'].concat(pluginDependencies.map(function(plugin) {
      return plugin.requirePackageName;
    }));

    requirejs(dependencies, function() {
      requirejs([],function() {
        window.define = undefined;
        window.require = undefined;
        camundaAdminUi(pluginDependencies);
      });
    });

  });

});

requirejs(['camunda-admin-bootstrap'], function(){});
