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
        '<%= pkg.gruntConfig.tasklistSourceDir %>/../../camunda-commons-ui/lib/**/*.less',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/../../camunda-commons-ui/resources/less/**/*.less',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/**/*.{css,less}'
      ],
      tasks: [
        'less:tasklist_styles'
      ]
  };

  watchConf.tasklist_scripts = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/**/*.{js,html,json}'
      ],
      tasks: [
        'localescompile:tasklist_locales',
        'requirejs:tasklist_scripts'
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
        '<%= pkg.gruntConfig.tasklistSourceDir %>/../../camunda-commons-ui/lib/**/*.{js,html}',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/../../camunda-commons-ui/{resources,lib/*}/locales/**/*.json',
        '<%= pkg.gruntConfig.tasklistSourceDir %>/../../camunda-commons-ui/node_modules/camunda-bpm-sdk-js/dist/**/*.js',
        '../../camunda-bpm-platform-ee/webapps/camunda-webapp/plugins/target/classes/plugin-webapp/**/*.{js,html}'
      ],
      tasks: [
        'localescompile:tasklist_locales',
        'requirejs:tasklist_scripts'
      ]
  };

  watchConf.tasklist_dist = {
    options: {
      cwd: '<%= pkg.gruntConfig.tasklistBuildTarget %>/',
      livereload: config.livereloadPort || false
    },
    files: '**/*.{css,html,js}'
  };

};
