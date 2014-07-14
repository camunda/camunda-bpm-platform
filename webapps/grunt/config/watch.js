module.exports = function(config) {

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
        // 'node_modules/camunda-commons-ui/grunt/config/less.js',
        '<%= pkg.gruntConfig.clientDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.clientDir %>/scripts/*/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        'grunt/config/require.js',
        'node_modules/camunda-bpm-sdk-js/dist/**/*.js',
        '<%= pkg.gruntConfig.clientDir %>/scripts/**/*.{js,html}'
      ],
      tasks: [
        'newer:jshint:scripts',
        'requirejs:scripts'
      ]
    },

    // sdk: {
    //   files: [
    //     // 'grunt/config/require.js',
    //     'node_modules/camunda-bpm-sdk-js/dist/**/*.js'
    //   ],
    //   tasks: [
    //     'copy:sdk',
    //     'requirejs:dependencies'
    //   ]
    // },

    unitTest: {
      files: [
        // 'grunt/config/jasmine_node.js',
        'test/unit/**/*Spec.js'
      ],
      tasks: [
        'jasmine_node:unit'
      ]
    },

    integrationTest: {
      files: [
        // 'grunt/config/karma.js',
        'test/integration/main.js',
        'test/integration/**/*Spec.js'
      ],
      tasks: [
        'karma:integration'
      ]
    },

    e2eTest: {
      files: [
        // 'grunt/config/protractor.js',
        'test/e2e/**/*Spec.js'
      ],
      tasks: [
        'protractor:e2e'
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
