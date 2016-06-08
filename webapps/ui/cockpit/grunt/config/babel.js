module.exports = function(config, babelConf) {
  'use strict';

  var options = {
    compact: true,
    presets: ['es2015']
  };

  babelConf.cockpit_scripts = {
    options: options,
    files: {
      '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/camunda-cockpit-ui.js': '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/camunda-cockpit-ui.js'
    }
  };

  babelConf.cockpit_plugins = {
    options: options,
    files: {
      '<%= pkg.gruntConfig.pluginBuildTarget %>/cockpit/app/plugin.js': '<%= pkg.gruntConfig.pluginBuildTarget %>/cockpit/app/plugin.js'
    }
  };

};
