
var config = {

  // base path, that will be used to
  // resolve files and exclude

  // we use /cockpit-webapp/src
  basePath: '../../..',
  frameworks: ['jasmine', 'requirejs' ],

  files: [
    { pattern: 'main/webapp/**/*.js', included: false },
    { pattern: 'test/js/unit/**/*.js', included: false },
    { pattern: 'test/js/lib/**/*.js', included: false },

    'test/js/config/require-unit-bootstrap.js'
  ],

  browsers: ["Chrome"], // "PhantomJS", "Firefox"

  autoWatch: true,

  junitReporter: {
    outputFile: '../../../../target/failsafe-reports/e2e.xml',
    suite: 'E2E'
  },

  plugins: [
    'karma-chrome-launcher',
    'karma-firefox-launcher',
    'karma-phantomjs-launcher',
    'karma-jasmine',
    'karma-requirejs'
  ]
};

for (var key in config) {

  this[key] = config[key];
}
