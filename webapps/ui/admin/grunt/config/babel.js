module.exports = function(config, babelConf) {
  'use strict';

  var options = {
    compact: true,
    presets: ['es2015']
  };

  babelConf.admin_scripts = {
    options: options,
    files: {
      '<%= pkg.gruntConfig.adminBuildTarget %>/scripts/camunda-admin-ui.js': '<%= pkg.gruntConfig.adminBuildTarget %>/scripts/camunda-admin-ui.js'
    }
  };

  babelConf.admin_plugins = {
    options: options,
    files: {
      '<%= pkg.gruntConfig.pluginBuildTarget %>/admin/app/plugin.js': '<%= pkg.gruntConfig.pluginBuildTarget %>/admin/app/plugin.js'
    }
  };

};
