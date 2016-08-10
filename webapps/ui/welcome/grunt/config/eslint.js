module.exports = function(config, eslintConf) {
  'use strict';

  eslintConf.welcome_scripts = {
    src: [
      '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/**/*.js'
    ]
  };

  eslintConf.welcome_plugins = {
    src: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/welcome/plugins/**/*.js'
    ]
  };

};
