module.exports = function(config, eslintConf) {
  'use strict';

  eslintConf.admin_scripts = {
    src: [
      '<%= pkg.gruntConfig.adminSourceDir %>/scripts/**/*.js'
    ]
  };

  eslintConf.admin_plugins = {
    src: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/admin/plugins/**/*.js'
    ]
  };

};
