// Testacular configuration
// Generated on Tue Feb 19 2013 16:25:15 GMT+0100 (CET)


// base path, that will be used to resolve files and exclude
basePath = '.';


// list of files / patterns to load in the browser
files = [
//  JASMINE,
//  JASMINE_ADAPTER,
  ANGULAR_SCENARIO,
  ANGULAR_SCENARIO_ADAPTER,
  'src/**/*-scenarios.js'
//  '../../main/webapp/assets/js/lib/jquery*js',
//  '../../main/webapp/assets/js/lib/angular/angular.js',
//  '../../main/webapp/assets/js/lib/angular/angular-resource.js',
//  '../../main/webapp/common/**/*js',
//  '../../main/webapp/app/**/*js',
//  'src/*js',
//  'lib/*js',
];


// list of files to exclude
exclude = [
  
];


// test results reporter to use
// possible values: 'dots', 'progress', 'junit'
reporters = ['progress', 'junit'];

  junitReporter = {
    // will be resolved to basePath (in the same way as files/exclude patterns)
    outputFile: 'test-results.xml'
  };

// web server port
// CLI --port 9876
port = 9876;

// cli runner port
// CLI --runner-port 9100
runnerPort = 9100;

// enable / disable colors in the output (reporters and logs)
colors = true;

// level of logging
// possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
logLevel = LOG_DEBUG;

// enable / disable watching file and executing tests whenever any file changes
autoWatch = true;

// Start these browsers, currently available:
// - Chrome
// - ChromeCanary
// - Firefox
// - Opera
// - Safari (only Mac)
// - PhantomJS
// - IE (only Windows)
browsers = ['Chrome'];

urlRoot = '/__testacular/'; 

proxies = {
  '/': 'http://localhost:8081/cockpit/',
  '/cockpit/': 'http://localhost:8081/cockpit/',
  '/engine-rest/': 'http://localhost:8081/engine-rest/'
};
