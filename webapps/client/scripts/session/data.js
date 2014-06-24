'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular'
], function(angular) {

  /**
   * @module cam.tasklist.session.data
   */

  /**
   * @memberof cam.tasklist.session
   */

  var sessionDataModule = angular.module('cam.tasklist.session.data', []);
  var ajax = angular.element.ajax;


  function replaceVars(str, vars) {
    for (var v in vars) {
      str = str.replace(new RegExp('{{'+ v +'}}', 'g'), vars[v]);
    }
    return str;
  }




  function CamLegacySessionData(config) {
    config = config || {};
    if (!config.defer) { throw new Error('defer must be passed in the configuration'); }
    this.defer = config.defer;
    this.baseUrl = config.baseUrl || '/camunda/api/admin/auth/user/default';
  }

  CamLegacySessionData.prototype.query = function(options) {
    options = options || {};

    var deferred = this.defer();
    var reqParams = {
      type:         options.type || 'GET',
      dataType:     'json',
      contentType:  'application/x-www-form-urlencoded',
      url:          this.baseUrl + replaceVars(options.path || '', options.instance || {})
    };

    if (options.data) {
      reqParams.data = options.data;
    }

    deferred.notify('request:start');

    ajax(reqParams)
    .done(function(data) {
      deferred.resolve(data);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      // console.warn('request error', errorThrown, stack);
      deferred.reject(errorThrown instanceof Error ? errorThrown : new Error(errorThrown));
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  CamLegacySessionData.prototype.create = function(username, password) {
    return this.query({
      type: 'POST',
      path: '/login/tasklist',
      data: {
        username: username,
        password: password
      }
    });
  };

  CamLegacySessionData.prototype.retrieve = function() {
    return this.query();
  };

  CamLegacySessionData.prototype.destroy = function(username, password) {
    return this.query({
      type: 'POST',
      path: '/logout'
    });
  };

  sessionDataModule.factory('camLegacySessionData', [
          '$q',
  function($q) {
    return new CamLegacySessionData({defer: $q.defer});
  }]);

  return sessionDataModule;
});
