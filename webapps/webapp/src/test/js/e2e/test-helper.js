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
    operations = {};
  } else if (arguments.length === 1 && typeof operations === 'object') {
    // testHelper(setupObject);
    noReset = false;
    done = function(){};
  } else if (arguments.length === 2 && typeof noReset === 'function'){
    // testHelper(setupObject, function(){ console.log('setup complete'); });
    done = noReset;
    noReset = false;
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
        body = JSON.parse(body);
        cb(null, body);
      });
    }
  ];
  
  keys(operations).forEach(function (resourceName) {
    keys(operations[resourceName]).forEach(function (methodName) {
      operations[resourceName][methodName].forEach(function (data) {
        var resource = new camClient.resource(resourceName);
        callbacks.push(function (cb) {
          console.info('doing '+resourceName+'.'+methodName, data);
          resource[methodName](data, function(){
            cb();
          });
        });
      });
    });
  });

  CamSDK.utils.series(callbacks, function() {
    deferred.fulfill();
  });

  return deferred.promise;
};
