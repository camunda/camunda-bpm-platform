'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
//            'angular', 'jquery', 'hyperagent'
// ], function(angular,   $,        Hyperagent) {
           'angular', 'jquery'
], function(angular,   $) {
  var taskDataModule = angular.module('cam.tasklist.task.data', []);
  taskDataModule.factory('camTaskHistoryData', function() {
    return function() {
      var items = [];
      // angular.forEach(fixturer.randomItem(_history, 8), function(hEvent, i) {
      //   var item = angular.copy(hEvent);
      //   item.timestamp = item.timestamp + fixturer.random(i * 200, i * 2000);
      //   item.operationType = fixturer.randomItem([
      //     'Claim',
      //     'Assign',
      //     'Complete',
      //     'Edit',
      //     'Comment'
      //   ]);
      //   items.push(item);
      // });
      return items;
    };
  });

  taskDataModule.factory('camTaskFormData', function() {
    return function() {
      var items = [];
      // angular.forEach(fixturer.randomItem(_fields, 6), function(field, i) {
      //   var item = angular.copy(field);
      //   item.name = item.name +'-'+ i;
      //   items.push(item);
      // });
      return items;
    };
  });

  // taskDataModule.factory('camTaskData', function() {
  //   return function() {
  //     var items = [];
  //     // angular.forEach(fixturer.randomItem(_fields, 6), function(field, i) {
  //     //   var item = angular.copy(field);
  //     //   item.name = item.name +'-'+ i;
  //     //   items.push(item);
  //     // });
  //     return items;
  //   };
  // });

  function CamTaskData(config) {
    config = config || {};
    if (!config.$q) { throw new Error('$q must be passed in the configuration'); }
    this.$q = config.$q;
  }

  CamTaskData.prototype.get =   function() {};

  CamTaskData.prototype.query = function(where) {
    console.info('load tasks where...', where);
    var deferred = this.$q.defer();

    deferred.notify('request:start');

    $.ajax({
      data: where,
      url: '/tasklist/tasks'
    })
    .done(function(data) {
      deferred.resolve(data);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      deferred.reject(textStatus);
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  // taskDataModule.factory('camTaskData', function() {
  //   // Configure Hyperagent to prefix every URL with the unicorn proxy.
  //   Hyperagent.configure('ajax', function(options) {
  //     options.url = 'https://unicorn-cors-proxy.herokuapp.com/' + options.url;

  //     return $.ajax(options);
  //   });

  //   return new Hyperagent.Resource({
  //     url: '/',
  //     headers: {
  //       // 'X-Requested-With': 'Hyperagent'
  //     }
  //   });
  // });

  taskDataModule.factory('camTaskData', [
          '$q',
  function($q) {
    return new CamTaskData({$q: $q});
  }]);

  return taskDataModule;
});
