module.exports = function(config, eslintConf) {
  'use strict';

  eslintConf.tasklist_scripts = {
    src: [
      '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/**/*.js'
    ]
  };

  eslintConf.tasklist_plugins = {
    src: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/tasklist/plugins/**/*.js'
    ]
  };

};
