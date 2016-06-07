__define('camunda-cockpit-bootstrap', [
  './scripts/camunda-cockpit-ui'
], function () {
  'use strict';

  var camundaCockpitUi = window.CamundaCockpitUi;

  requirejs.config({
    baseUrl: '../../../lib'
  });

  var requirePackages = window;
  camundaCockpitUi.exposePackages(requirePackages);

  window.define = window.__define;
  window.require = window.__require;

  requirejs(['globalize'], function(globalize) {

    globalize(requirejs, ['angular', 'camunda-commons-ui', 'camunda-bpm-sdk-js', 'jquery', 'angular-data-depend', 'moment', 'events'], requirePackages);

    var pluginPackages = window.PLUGIN_PACKAGES || [];
    var pluginDependencies = window.PLUGIN_DEPENDENCIES || [];

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
        camundaCockpitUi(pluginDependencies);
      });
    });

  });

});

requirejs(['camunda-cockpit-bootstrap'], function(){});
