'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
//            'angular', 'jquery', 'hyperagent'
// ], function(angular,   $,        Hyperagent) {
           'angular', 'jquery'
], function(angular,   $) {
  var sessionDataModule = angular.module('cam.tasklist.session.data', []);


  function CamSessionData(config) {
    config = config || {};
    if (!config.defer) { throw new Error('defer must be passed in the configuration'); }
    this.defer = config.defer;
  }

  CamSessionData.prototype.get   = function(id, options) {
    var deferred = this.defer();
    options = options || {};
    var session;
    var query = {};
    if (id.id) {
      session = id;
      id = session.id;
    }


    $.ajax({
      url: '/tasklist/sessions/'+ id,
      data: query
    })
    .done(function(data) {
      deferred.resolve(data._embedded.sessions);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      deferred.reject(textStatus);
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  CamSessionData.prototype.query = function(options) {
    var deferred = this.defer();
    options = options || {};

    deferred.notify('request:start');

    var query = {};
    if (options.session) {
      query.session = options.session.id || options.session;
    }

    $.ajax({
      url: '/tasklist/sessions',
      data: query
    })
    .done(function(data) {
      deferred.resolve(data._embedded.sessions);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      deferred.reject(textStatus);
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  sessionDataModule.factory('camSessionData', [
          '$q',
  function($q) {
    return new CamSessionData({$q: $q});
  }]);

  return sessionDataModule;
});
