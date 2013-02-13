// Testacular configuration
// Generated on Wed Nov 07 2012 11:50:04 GMT+0100 (W. Europe Standard Time)


// base path, that will be used to resolve files and exclude
basePath = '../../../';


// list of files / patterns to load in the browser
files = [
  JASMINE,
  JASMINE_ADAPTER,
  'src/main/webapp/assets/js/lib/angular/angular.js',
  'src/main/webapp/assets/js/lib/angular/angular-resource.js',
  'src/test/javascript/lib/angular/angular-mocks.js',
  
  // actual application files
  'src/main/webapp/assets/js/app/*.js',
  
  // spec files
  'src/test/javascript/spec/**/*Spec.js'
];

// list of files to exclude
exclude = [
  'src/main/webapp/assets/js/lib/dojo/**/*.js',
];


// test results reporter to use
// possible values: 'dots', 'progress', 'junit'
reporters = ['progress'];


// web server port
port = 8080;


// cli runner port
runnerPort = 9100;


// enable / disable colors in the output (reporters and logs)
colors = true;


// level of logging
// possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
logLevel = LOG_INFO;


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
browsers = ['IE'];


// If browser does not capture in given timeout [ms], kill it
captureTimeout = 5000;


// Continuous Integration mode
// if true, it capture browsers, run tests and exit
singleRun = true;
