module.exports = function(config) {
  config = config || {};

  return {
    options: {
      livereload: false
    },

    assets: {
      files: [
        'client/{fonts,images}/**/*',
        'client/*.html',
        'client/favicon.ico'
      ],
      tasks: [
        'copy:assets'
      ]
    },

    styles: {
      files: [
        'client/styles/**/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        'grunt/config/requirejs.js',
        'client/scripts/**/*.{js,html}'
      ],
      tasks: [
        'newer:jshint',
        'requirejs:scripts'
      ]
    },

    unitTest: {
      files: [
        'grunt/config/jasmine_node.js',
        'test/unit/**/*Spec.js'
      ],
      tasks: [
        'jasmine_node:unit'
      ]
    },

    integrationTest: {
      files: [
        'grunt/config/karma.js',
        'test/integration/main.js',
        'test/integration/**/*Spec.js'
      ],
      tasks: [
        'karma:integration'
      ]
    },

    e2eTest: {
      files: [
        'grunt/config/protractor.js',
        'test/e2e/**/*Spec.js'
      ],
      tasks: [
        'protractor:e2e'
      ]
    },

    served: {
      files: ['dist/**/*'],
      tasks: [],
      options: {
        livereload: parseInt(config.livereloadPort || 35729)
      }
    }
  };
};
