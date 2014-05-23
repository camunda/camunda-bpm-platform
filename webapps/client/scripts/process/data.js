'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
//            'angular', 'jquery', 'hyperagent'
// ], function(angular,   $,        Hyperagent) {
           'angular', 'jquery'
], function(angular,   $) {
  var processDataModule = angular.module('cam.tasklist.process.data', []);



  function replaceVars(str, vars) {
    for (var v in vars) {
      str = str.replace(new RegExp('{{'+ v +'}}', 'g'), vars[v]);
    }
    return str;
  }










  function CamLegacyProcessData(config) {
    config = config || {};
    if (!config.defer) { throw new Error('defer must be passed in the configuration'); }
    this.defer = config.defer;
    this.baseUrl = config.baseUrl || '/camunda/api/engine/engine/default';
  }

  CamLegacyProcessData.prototype.query = function(options) {
    options = options || {};

    var deferred = this.defer();
    var reqParams = {
      type:         options.type || 'GET',
      dataType:     'json',
      contentType:  'application/json;charset=UTF-8',
      url:          this.baseUrl + replaceVars(options.path || '', options.instance || {})
    };

    if (options.data) {
      reqParams.data = JSON.stringify(options.data);
    }

    // console.info('request parameters', reqParams);

    deferred.notify('request:start');

    $.ajax(reqParams)
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

  CamLegacyProcessData.prototype.list = function(where) {
    where = where || {};
    return this.query({
      path: '/process-definition'
    });
  };

  CamLegacyProcessData.prototype.count = function(where) {
    where = where || {};
    return this.query({
      path: '/process-definition/count'
    });
  };

  CamLegacyProcessData.prototype.byId = function(id) {
    return this.query({
      path: '/process-definition/{{id}}',
      instance: {
        id: id
      }
    });
  };

  CamLegacyProcessData.prototype.byKey = function(key) {
    return this.query({
      path: '/process-definition/key/{{key}}',
      instance: {
        key: key
      }
    });
  };

  CamLegacyProcessData.prototype.start = function(key, options) {
    options = options || {};
    options.data = options.data || {variables: {}};

    return this.query({
      type: 'POST',
      path: '/process-definition/key/{{key}}/start',
      instance: {
        key: key
      },
      data: options.data
    });
  };

  processDataModule.factory('camLegacyProcessData', [
          '$q',
  function($q) {
    return new CamLegacyProcessData({defer: $q.defer});
  }]);













  function CamProcessData(config) {
    config = config || {};
    if (!config.defer) { throw new Error('defer must be passed in the configuration'); }
    this.defer = config.defer;
  }

  CamProcessData.prototype.get   = function(id, options) {
    var deferred = this.defer();
    options = options || {};
    var process;
    var query = {};
    if (id.id) {
      process = id;
      id = process.id;
    }


    $.ajax({
      url: '/tasklist/processes/'+ id,
      data: query
    })
    .done(function(data) {
      deferred.resolve(data._embedded.processes);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      deferred.reject(textStatus);
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  CamProcessData.prototype.query = function(options) {
    var deferred = this.defer();
    options = options || {};

    deferred.notify('request:start');

    var query = {};
    if (options.process) {
      query.process = options.process.id || options.process;
    }

    $.ajax({
      url: '/tasklist/processes',
      data: query
    })
    .done(function(data) {
      deferred.resolve(data._embedded.processes);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      deferred.reject(textStatus);
    })
    .always(function() {
      deferred.notify('request:complete');
    });

    return deferred.promise;
  };

  processDataModule.factory('camProcessData', [
          '$q',
  function($q) {
    return new CamProcessData({defer: $q.defer});
  }]);








  return processDataModule;
});
