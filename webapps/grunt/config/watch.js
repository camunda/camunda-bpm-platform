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
      files: ['<%= buildTarget %>/**/*'],
      options: {
        livereload: config.livereloadPort || false
      }
    }
  };
};
