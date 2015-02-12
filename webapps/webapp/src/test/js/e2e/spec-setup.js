'use strict';

var resetUrl = 'http://localhost:8080/camunda/ensureCleanDb/default';
var request = require('request');
var CamSDK = require('camunda-bpm-sdk-js');
var camClient = new CamSDK.Client({
  mock: false,
  apiUri: 'http://localhost:8080/engine-rest'
});

module.exports = function (operations) {
  var callbacks = [
    function (cb) {
      request(resetUrl, function(err, res, body) {
        if (err) {
          return cb(err);
        }

        var body = JSON.parse(body);
        cb(null, body);
      });
    }
  ];

  for (var resourceName in operations) {
    for (var methodName in operations[resourceName]) {
      var resource = new camClient.resource(resourceName);
      operations[resourceName][methodName].forEach(function (data) {
        callbacks.push(function (cb) {
          console.info('doing '+resourceName+'.'+methodName+'', data);
          resource[methodName](data, cb);
        });
      });
    }
  }


  CamSDK.utils.series(callbacks, function (err, results) {
      if (err) {
        throw err;
      }
      console.info('operation results', results);
  });
};
