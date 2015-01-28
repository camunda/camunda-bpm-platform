module.exports = function(config) {
  'use strict';
  return {
    options: {
      livereload: false
    },

    assets: {
      files: [
        '<%= pkg.gruntConfig.clientDir %>/{fonts,images}/**/*',
        '<%= pkg.gruntConfig.clientDir %>/index.html',
        '<%= pkg.gruntConfig.clientDir %>/favicon.ico'
      ],
      tasks: [
        'newer:copy:assets'
      ]
    },

    styles: {
      files: [
        'node_modules/camunda-commons-ui/lib/widgets/**/*.less',
        'node_modules/camunda-commons-ui/resources/less/**/*.less',
        '<%= pkg.gruntConfig.clientDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.clientDir %>/scripts/**/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        '<%= pkg.gruntConfig.clientDir %>/scripts/**/*.{js,html,json}'
      ],
      tasks: [
        'newer:jshint:scripts',
        'localescompile',
        'requirejs:scripts'
      ]
    },

    config: {
      files: [
        '<%= pkg.gruntConfig.clientDir %>/scripts/config/config.js'
      ],
      tasks: [
        'copy:config'
      ]
    },

    sdk: {
      files: [
        'node_modules/camunda-commons-ui/lib/**/*.js',
        'node_modules/camunda-commons-ui/{resources,lib/*}/locales/**/*.json',
        'node_modules/camunda-bpm-sdk-js/dist/**/*.js'
      ],
      tasks: [
        'localescompile',
        'requirejs:scripts',
        'requirejs:dependencies'
      ]
    },

    served: {
      options: {
        cwd: '<%= buildTarget %>/',
        livereload: config.livereloadPort || false
      },
      files: '**/*.{css,html,js}'
    }
  };
};
