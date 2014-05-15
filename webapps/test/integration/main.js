(function(root) {
  'use strict';

  var tests = ['/base/dist/scripts/deps.js'];
  for (var file in root.__karma__.files) {
    if (root.__karma__.files.hasOwnProperty(file)) {
      if (/Spec\.js$/.test(file)) {
        tests.push(file);
      }
    }
  }

  requirejs.config({
    baseUrl: '/base',

    paths: {
      'camunda-tasklist': '/base/dist/scripts/camunda-tasklist'
    }
  });

  setTimeout(function() {
    requirejs(tests, function() {
      root.__karma__.start();
    }, function(err) {
      console.error('Something went wrong while loading the tests.', err.stack);
    });
  }, 200);
}(this));
