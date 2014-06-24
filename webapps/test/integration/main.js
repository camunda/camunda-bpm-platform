(function(root) {
  'use strict';

  var tests = [];
  for (var file in root.__karma__.files) {
    if (root.__karma__.files.hasOwnProperty(file)) {
      if (/Spec\.js$/.test(file)) {
        tests.push(file);
      }
    }
  }

  require(['/base/client/scripts/rjsconf.js'], function(conf) {
    conf.baseUrl = '/base/client';

    requirejs.config(conf);

    require(tests, function() {
      root.__karma__.start();
    }, function(err) {
      console.error('Something went wrong while loading the tests.', err.stack);
    });
  });
}(this));
