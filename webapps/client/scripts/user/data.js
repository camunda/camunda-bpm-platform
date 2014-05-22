'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
//            'angular', 'jquery', 'hyperagent'
// ], function(angular,   $,        Hyperagent) {
           'angular', 'jquery'
], function(angular,   $) {
  var userDataModule = angular.module('cam.tasklist.user.data', []);


  function CamUserData(config) {
    config = config || {};
    if (!config.defer) { throw new Error('defer must be passed in the configuration'); }
    this.defer = config.defer;
  }

  CamUserData.prototype.get   = function(id, options) {
    var deferred = this.defer();
    options = options || {};
    var user;
    var query = {};
    if (id.id) {
      user = id;
      id = user.id;
    }


    $.ajax({
      url: '/tasklist/users/'+ id,
      data: query
    })
    .done(function(data) {
      deferred.resolve(data._embedded.users);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      deferred.reject(textStatus);
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  CamUserData.prototype.query = function(options) {
    var deferred = this.defer();
    options = options || {};

    deferred.notify('request:start');

    var query = {};
    if (options.user) {
      query.user = options.user.id || options.user;
    }

    $.ajax({
      url: '/tasklist/users',
      data: query
    })
    .done(function(data) {
      deferred.resolve(data._embedded.users);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      deferred.reject(textStatus);
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  userDataModule.factory('camUserData', [
          '$q',
  function($q) {
    return new CamUserData({$q: $q});
  }]);

  return userDataModule;
});
