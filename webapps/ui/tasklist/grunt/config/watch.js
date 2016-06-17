module.exports = function(config, watchConf) {
  'use strict';

  var options = {
      livereload: false
  };

  watchConf.tasklist_assets = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.tasklistSourceDir %>/{fonts,images}/**/*',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/index.html',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/favicon.ico'
      ],
      tasks: [
        'copy:tasklist_assets',
        'copy:tasklist_index'
      ]
  };

  watchConf.tasklist_styles = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.tasklistSourceDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/**/*.{css,less}'
      ],
      tasks: [
        'less:tasklist_styles'
      ]
  };

  watchConf.tasklist_plugin_styles = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.pluginSourceDir %>/tasklist/plugins/**/*.{css,less}'
      ],
      tasks: [
        'less:tasklist_plugin_styles'
      ]
  };

  watchConf.tasklist_config = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/config/config.js'
      ],
      tasks: [
        'copy:tasklist_config'
      ]
  };

  watchConf.tasklist_dependencies = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.tasklistSourceDir %>/../../camunda-commons-ui/{resources,lib/*}/locales/**/*.json',
      ],
      tasks: [
        'localescompile:tasklist_locales'
      ]
  };

  watchConf.tasklist_scripts_lint = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/**/*.js'
    ],
    tasks: [
      'newer:eslint:tasklist_scripts'
    ]
  };

  watchConf.tasklist_plugins_lint = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/tasklist/plugins/**/*.js'
    ],
    tasks: [
      'newer:eslint:tasklist_plugins'
    ]
  };

  watchConf.tasklist_dist = {
    options: {
      livereload: config.livereloadPort || false
    },
    files: [
      '<%= pkg.gruntConfig.tasklistBuildTarget %>/**/*.{css,html,js}',
      '<%= pkg.gruntConfig.pluginBuildTarget %>/tasklist/**/*.{css,html,js}'
    ]
  };
};
