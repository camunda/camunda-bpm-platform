module.exports = function(karma) {
  var conf = require('./../../../../src/main/webapp/require-conf');

  karma.set({
    basePath: '../../../..',

    frameworks: [
      'jasmine',
      'requirejs'
    ],

    files: [
      { pattern: 'target/webapp{,/**}/*.js', included: false },

      'src/test/js/config/require-test-bootstrap.js',
      { pattern: 'src/test/js/test/**/*.js', included: false },
    ],

    browsers: ['PhantomJS'],

    autoWatch: true,

    // junitReporter: {
    //   outputFile: '../../../../target/failsafe-reports/e2e.xml',
    //   suite: 'E2E'
    // },

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
