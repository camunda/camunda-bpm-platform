/* jshint node: true */
'use strict';
var path = require('path');

xdescribe('The environment', function() {
  var tasklist;
  var rjs;

  beforeEach(function(done) {
    rjs = require('requirejs');
    rjs([
      'client/scripts/rjsconf'
    ], function(conf) {
      var origBaseUrl = conf.baseUrl;
      // conf.paths.jquery = path.join(path.dirname(require.resolve('cheerio')), 'index');
      conf.baseUrl = path.join(process.cwd(), 'client');
      rjs.config(conf);
      done();
    }, done);
  });

  it('loads with requirejs', function(done) {
    rjs(['camunda-tasklist-ui/controls'], function(loaded) {
      console.info('loaded', loaded);

      done();
    }, done);
  });

  xit('has what it needs', function() {
    console.info('Try to do something clever for once...', Object.keys(tasklist));
  });
});

