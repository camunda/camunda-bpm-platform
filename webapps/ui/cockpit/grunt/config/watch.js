module.exports = function(config, watchConf) {
  'use strict';

  var options = {
      livereload: false
  };


  watchConf.cockpit_assets = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.cockpitSourceDir %>/{fonts,images}/**/*',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/index.html',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/favicon.ico'
      ],
      tasks: [
        'copy:cockpit_assets',
        'copy:cockpit_index'
      ]
  };

  watchConf.cockpit_styles = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.cockpitSourceDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/**/*.{css,less}'
      ],
      tasks: [
        'less:cockpit_styles',
        'less:cockpit_styles_components'
      ]
  };

  watchConf.cockpit_plugin_styles = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/**/*.{css,less}'
      ],
      tasks: [
        'less:cockpit_plugin_styles'
      ]
  };

  watchConf.cockpit_scripts_lint = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/**/*.js'
    ],
    tasks: [
      'newer:eslint:cockpit_scripts'
    ]
  };

  watchConf.cockpit_plugins_lint = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/**/*.js'
    ],
    tasks: [
      'newer:eslint:cockpit_plugins'
    ]
  };


  watchConf.cockpit_dist = {
    options: {
      livereload: config.livereloadPort || false
    },
    files: [
      '<%= pkg.gruntConfig.cockpitBuildTarget %>/**/*.{css,html,js}',
      '<%= pkg.gruntConfig.pluginBuildTarget %>/cockpit/**/*.{css,html,js}'
    ]
  };
};
