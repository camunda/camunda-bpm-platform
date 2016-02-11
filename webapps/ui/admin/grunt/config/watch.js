module.exports = function(config, watchConf) {
  'use strict';

  var options = {
      livereload: false
  };

  watchConf.admin_assets = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.adminSourceDir %>/{fonts,images}/**/*',
        '<%= pkg.gruntConfig.adminSourceDir %>/scripts/index.html',
        '<%= pkg.gruntConfig.adminSourceDir %>/scripts/favicon.ico'
      ],
      tasks: [
        'copy:admin_assets',
        'copy:admin_index'
      ]
  };

  watchConf.admin_styles = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.adminSourceDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.adminSourceDir %>/scripts/**/*.{css,less}'
      ],
      tasks: [
        'less:admin_styles'
      ]
  };

  watchConf.admin_dist = {
    options: {
      cwd: '<%= pkg.gruntConfig.adminBuildTarget %>/',
      livereload: config.livereloadPort || false
    },
    files: '**/*.{css,html,js}'
  };

};
