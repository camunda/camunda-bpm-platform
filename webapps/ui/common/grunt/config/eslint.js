module.exports = function(config, eslintConf) {
  'use strict';

  eslintConf.webapp_common = {
    src: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/common/scripts/**/*.js'
    ]
  };

};
