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
        '<%= pkg.gruntConfig.clientDir %>/scripts/*/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        '<%= pkg.gruntConfig.clientDir %>/node_modules/camunda-*/**/*.{js,html}',
        '<%= pkg.gruntConfig.clientDir %>/bower_components/camunda-*/**/*.{js,html}',
        '<%= pkg.gruntConfig.clientDir %>/scripts/**/*.{js,html}'
      ],
      tasks: [
        // 'newer:jshint:scripts',
        // 'requirejs:dependencies',
        'requirejs:scripts'
      ]
    },

    // sdk: {
    //   files: [
    //     'node_modules/camunda-bpm-sdk-js/dist/**/*.js'
    //   ],
    //   tasks: [
    //     'copy:sdk',
    //     'requirejs:scripts'
    //   ]
    // },

    // commons: {
    //   files: [
    //     'node_modules/camunda-commons/lib/**/*.{js,html}'
    //   ],
    //   tasks: [
    //     'requirejs:scripts'
    //   ]
    // },

    // unitTest: {
    //   files: [
    //     'grunt/config/jasmine_node.js',
    //     'test/unit/**/*Spec.js'
    //   ],
    //   tasks: [
    //     'jasmine_node:unit'
    //   ]
    // },

    // integrationTest: {
    //   files: [
    //     'grunt/config/karma.js',
    //     'test/integration/main.js',
    //     'test/integration/**/*Spec.js'
    //   ],
    //   tasks: [
    //     'karma:integration'
    //   ]
    // },

    // e2eTest: {
    //   files: [
    //     'grunt/config/protractor.js',
    //     'test/e2e/**/*Spec.js'
    //   ],
    //   tasks: [
    //     'protractor:e2e'
    //   ]
    // },

    served: {
      files: ['<%= buildTarget %>/**/*.*'],
      options: {
        livereload: config.livereloadPort || false
      }
    }
  };
};
