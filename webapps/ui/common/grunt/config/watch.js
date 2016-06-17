module.exports = function(config, watchConf) {
  'use strict';

  var options = {
      livereload: false
  };

  watchConf.webapp_common_lint = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/common/scripts/**/*.js'
    ],
    tasks: [
      'newer:eslint:webapp_common'
    ]
  };
};
