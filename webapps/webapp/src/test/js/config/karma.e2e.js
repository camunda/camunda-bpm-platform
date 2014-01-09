module.exports = function(karma) {
  karma.set({
    frameworks: ['ng-scenario'],

    files: [
      // add require/amd support
      'require-e2e-adapter.js',
      '../e2e/cockpit-scenario.js'
    ],

    browsers: ["PhantomJS", "Firefox", "Chrome"],

    autoWatch: false,

    junitReporter: {
      outputFile: '../../../../target/failsafe-reports/e2e.xml',
      suite: 'E2E'
    },

    // Proxying the original application due to same origin policy constraints
    // https://github.com/karma-runner/karma/issues/179
    urlRoot: '/__karma/',

    proxies: {
      '/': 'http://localhost:8080'
    },

    plugins: [
      'karma-chrome-launcher',
      'karma-firefox-launcher',
      'karma-phantomjs-launcher',
      'karma-ng-scenario'
    ]
  });
};
