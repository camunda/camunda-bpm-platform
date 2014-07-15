module.exports = function(config) {

  return {
    options: {
      livereload: false
    },

    assets: {
      files: [
        config.clientDir +'/{fonts,images}/**/*',
        config.clientDir +'/index.html',
        config.clientDir +'/favicon.ico'
      ],
      tasks: [
        'newer:copy:assets'
      ]
    },

    styles: {
      files: [
        config.clientDir +'/styles/**/*.{css,less}',
        config.clientDir +'/scripts/*/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        // 'grunt/config/requirejs.js',
        config.clientDir +'/scripts/**/*.{js,html}'
      ],
      tasks: [
        // 'newer:jshint:scripts',
        // 'requirejs:dependencies',
        'requirejs:scripts'
      ]
    },

    sdk: {
      files: [
        'node_modules/camunda-bpm-sdk-js/dist/**/*.js'
      ],
      tasks: [
        'copy:sdk',
        'requirejs:scripts'
      ]
    },

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
