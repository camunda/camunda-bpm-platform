module.exports = function(config, watchConf) {
  'use strict';

  var options = {
      livereload: false
  };


  watchConf.welcome_assets = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.welcomeSourceDir %>/{fonts,images}/**/*',
        '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/index.html',
        '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/favicon.ico'
      ],
      tasks: [
        'copy:welcome_assets',
        'copy:welcome_index'
      ]
  };

  watchConf.welcome_styles = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.welcomeSourceDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/**/*.{css,less}'
      ],
      tasks: [
        'less:welcome_styles'
      ]
  };

  watchConf.welcome_plugin_styles = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.pluginSourceDir %>/welcome/plugins/**/*.{css,less}'
      ],
      tasks: [
        'less:welcome_plugin_styles'
      ]
  };

  watchConf.welcome_scripts_lint = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/**/*.js'
    ],
    tasks: [
      'newer:eslint:welcome_scripts'
    ]
  };

  watchConf.welcome_plugins_lint = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/welcome/plugins/**/*.js'
    ],
    tasks: [
      'newer:eslint:welcome_plugins'
    ]
  };


  watchConf.welcome_dist = {
    options: {
      livereload: config.livereloadPort || false
    },
    files: [
      '<%= pkg.gruntConfig.welcomeBuildTarget %>/**/*.{css,html,js}',
      '<%= pkg.gruntConfig.pluginBuildTarget %>/welcome/**/*.{css,html,js}'
    ]
  };
};
