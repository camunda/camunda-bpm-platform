module.exports = function(karma) {
  karma.set({

    // base path, that will be used to
    // resolve files and exclude

    // we use /camunda-webapp/src
    basePath: '../../..',
    frameworks: ['jasmine', 'requirejs' ],

    files: [
      { pattern: 'main/webapp/**/*.js', included: false },
      { pattern: 'test/js/unit/**/*.js', included: false },
      { pattern: 'test/js/lib/**/*.js', included: false },

      'test/js/config/require-unit-bootstrap.js'
    ],

    browsers: ['Chrome', 'IE'], // "PhantomJS", "Firefox"

    autoWatch: true,

    junitReporter: {
      outputFile: '../../../../target/failsafe-reports/e2e.xml',
      suite: 'E2E'
    },

    plugins: [
      'karma-ie-launcher',
      'karma-chrome-launcher',
      'karma-firefox-launcher',
      'karma-phantomjs-launcher',
      'karma-jasmine',
      'karma-requirejs'
    ]
  });
};
