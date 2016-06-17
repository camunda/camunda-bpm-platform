module.exports = function(config, eslintConf) {
  'use strict';

  eslintConf.cockpit_scripts = {
    src: [
      '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/**/*.js'
    ]
  };

  eslintConf.cockpit_plugins = {
    src: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/**/*.js'
    ]
  };

};
