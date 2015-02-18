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

  if (arguments.length === 1) {
    done = operations;
    noReset = false;
    operations = {};
  }

  if (arguments.length === 2) {
    done = noReset;
    noReset = false;
  }
  
  operations = operations || {};
  var callbacks = [
    function (cb) {
      if (noReset) {
        return cb();
      }
      request(resetUrl, function(err, res, body) {
        if (err) {
          return cb(err);
        }

        var body = JSON.parse(body);
        cb(null, body);
      });
    }
  ];
  
  keys(operations).forEach(function (resourceName) {
    keys(operations[resourceName]).forEach(function (methodName) {
      operations[resourceName][methodName].forEach(function (data) {
        var resource = new camClient.resource(resourceName);
        callbacks.push(function (cb) {
          console.info('doing '+resourceName+'.'+methodName+'', data);
          resource[methodName](data, cb);
        });
      });
    });
  });

  CamSDK.utils.series(callbacks, done);
};
