module.exports = function(config, babelConf) {
  'use strict';

  var options = {
    compact: true,
    presets: ['es2015']
  };

  babelConf.tasklist_scripts = {
    options: options,
    files: {
      '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/camunda-tasklist-ui.js': '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/camunda-tasklist-ui.js'
    }
  };

  babelConf.tasklist_plugins = {
    options: options,
    files: {
      '<%= pkg.gruntConfig.pluginBuildTarget %>/tasklist/app/plugin.js': '<%= pkg.gruntConfig.pluginBuildTarget %>/tasklist/app/plugin.js'
    }
  };

};
