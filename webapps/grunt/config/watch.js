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
        '<%= pkg.gruntConfig.cockpitSourceDir %>/../node_modules/camunda-commons-ui/lib/**/*.less',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/../node_modules/camunda-commons-ui/resources/less/**/*.less',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/**/*.{css,less}'
      ],
      tasks: [
        'less:cockpit_styles'
      ]
  };

  watchConf.cockpit_scripts = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/**/*.{js,html,json}'
      ],
      tasks: [
        'requirejs:cockpit_scripts'
      ]
  };

  watchConf.cockpit_dependencies = {
      options: options,
      files: [
        '<%= pkg.gruntConfig.cockpitSourceDir %>/../node_modules/camunda-commons-ui/lib/**/*.{js,html}',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/../node_modules/camunda-commons-ui/{resources,lib/*}/locales/**/*.json',
        '<%= pkg.gruntConfig.cockpitSourceDir %>/../node_modules/camunda-commons-ui/node_modules/camunda-bpm-sdk-js/dist/**/*.js',
      ],
      tasks: [
        'requirejs:cockpit_scripts'
      ]
  };

  watchConf.cockpit_dist = {
    options: {
      cwd: '<%= pkg.gruntConfig.cockpitBuildTarget %>/',
      livereload: config.livereloadPort || false
    },
    files: '**/*.{css,html,js}'
  };

};
