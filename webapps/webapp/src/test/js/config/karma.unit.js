module.exports = function(karma) {
  var conf = require('./../../../../src/main/webapp/require-conf');

  karma.set({
    basePath: '../../../..',

    frameworks: [
      'jasmine',
      'requirejs'
    ],

    files: [
      { pattern: 'target/webapp/**/*.js', included: false },
      { pattern: 'src/test/js/unit/**/*.js', included: false },
      { pattern: 'src/test/js/lib/**/*.js', included: false },

      'src/test/js/config/require-unit-bootstrap.js'
    ],

    browsers: ['PhantomJS'],

    singleRun: true,
    autoWatch: false,

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
