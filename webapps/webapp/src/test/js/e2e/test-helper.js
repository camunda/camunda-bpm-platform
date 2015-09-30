'use strict';

var resetUrl = 'http://localhost:8080/camunda/ensureCleanDb/default';
var request = require('request');
var CamSDK = require('camunda-bpm-sdk-js');
var camClient = new CamSDK.Client({
  mock: false,
  apiUri: 'http://localhost:8080/engine-rest'
});

var keys = Object.keys;

module.exports = function (operations, noReset, done) {
  var deferred = protractor.promise.defer();

  if (arguments.length === 1 && typeof operations === 'function') {
    // testHelper(function(){ console.log('setup complete'); });
    done = operations;
    noReset = false;
    operations = [];
  } else if (arguments.length === 1 && typeof operations === 'object') {
    // testHelper(setupObject);
    noReset = false;
    done = function(){};
  } else if (arguments.length === 2 && typeof noReset === 'function') {
    // testHelper(setupObject, function(err, result){ console.log('setup complete', result); });
    done = noReset;
    noReset = false;
  } else if (arguments.length === 2 && typeof noReset === 'boolean') {
    done = function(){};
  }

  var callbacks = [
    function (cb) {
      if (noReset) {
        return cb();
      }

      browser.manage().deleteAllCookies();

      request(resetUrl, function(err, res, body) {
        if (err) {
          return cb(err);
        }

        try {
          body = JSON.parse(body);
          cb(null, body);
        }
        catch (err) {
          cb(err);
        }
      });
    }
  ];

  operations.forEach(function(operation) {
    var resource = new camClient.resource(operation.module);
    callbacks.push(function (cb) {
      console.info('doing '+operation.module+'.'+operation.operation);//, operation.params);
      resource[operation.operation](operation.params, function(){
        cb();
      });
    });
  });

  CamSDK.utils.series(callbacks, function(err, result) {
    // now all process instances are started, we can start the jobs to create incidents
    // This method sets retries to 0 for all jobs that were created in the test setup
    if(err) {
      done(err, result);
      deferred.reject();
    }

    var resource = new camClient.resource('job');

    resource.list({}, function(err, result) {
      var jobTasks = [];
      for(var i = 0; i < result.length; i++) {
        jobTasks.push((function(i) {
          return function(cb) {
            resource.setRetries({
              id: result[i].id,
              retries: 0
            }, cb);
          };
        })(i));
      }
      CamSDK.utils.series(jobTasks, function(err, result) {
        done(err, result);
        deferred.fulfill();
      });
    });
  });

  return deferred.promise;
};
