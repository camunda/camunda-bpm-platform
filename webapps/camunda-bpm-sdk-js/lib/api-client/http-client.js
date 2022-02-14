/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var request = require('superagent');
var Q = require('q');
var Events = require('./../events');
var utils = require('./../utils');

/**
 * No-Op callback
 */
function noop() {}

/**
 * HttpClient
 *
 * A HTTP request abstraction layer to be used in node.js / browsers environments.
 *
 * @class
 * @memberof CamSDK.client
 */
var HttpClient = function(config) {
  config = config || {};

  config.headers = config.headers || {};
  if (!config.headers.Accept) {
    config.headers.Accept = 'application/hal+json, application/json; q=0.5';
  }

  if (!config.baseUrl) {
    throw new Error('HttpClient needs a `baseUrl` configuration property.');
  }

  Events.attach(this);

  this.config = config;
};

function end(self, done, deferred) {
  done = done || noop;
  return function(err, response) {
    // TODO: investigate the possible problems related to response without content
    if (err || (!response.ok && !response.noContent)) {
      err =
        err ||
        response.error ||
        new Error(
          'The ' +
            response.req.method +
            ' request on ' +
            response.req.url +
            ' failed'
        );
      if (response && response.body) {
        if (response.body.message) {
          err.message = response.body.message;
        }
      }
      self.trigger('error', err);
      if (deferred) {
        deferred.reject(err);
      }
      return done(err);
    }

    // superagent puts the parsed data into a property named "body"
    // and the "raw" content in property named "text"
    // and.. it does not parse the response if it does not have
    // the "application/json" type.
    if (response.type === 'application/hal+json') {
      if (!response.body || Object.keys(response.body).length === 0) {
        response.body = JSON.parse(response.text);
      }

      // and process embedded resources
      response.body = utils.solveHALEmbedded(response.body);
    }

    if (deferred) {
      deferred.resolve(
        response.body ? response.body : response.text ? response.text : ''
      );
    }
    done(
      null,
      response.body ? response.body : response.text ? response.text : ''
    );
  };
}

/**
 * Performs a POST HTTP request
 */
HttpClient.prototype.post = function(path, options) {
  options = options || {};
  var done = options.done || noop;
  var self = this;
  var deferred = Q.defer();
  var url = this.config.baseUrl + (path ? '/' + path : '');
  var req = request.post(url);

  var headers = options.headers || this.config.headers;
  headers.Accept = headers.Accept || this.config.headers.Accept;

  var isFieldOrAttach = false;
  // Buffer object is only available in node.js environement
  if (typeof Buffer !== 'undefined') {
    Object.keys(options.fields || {}).forEach(function(field) {
      req.field(field, options.fields[field]);
      isFieldOrAttach = true;
    });
    (options.attachments || []).forEach(function(file, idx) {
      req.attach('data_' + idx, new Buffer(file.content), file.name);
      isFieldOrAttach = true;
    });
  } else if (!!options.fields || !!options.attachments) {
    var err = new Error(
      'Multipart request is only supported in node.js environement.'
    );
    done(err);
    return deferred.reject(err);
  }

  if (!isFieldOrAttach) {
    req.send(options.data || {});
  }

  req.set(headers).query(options.query || {});

  req.end(end(self, done, deferred));
  return deferred.promise;
};

/**
 * Performs a GET HTTP request
 */
HttpClient.prototype.get = function(path, options) {
  var url = this.config.baseUrl + (path ? '/' + path : '');
  return this.load(url, options);
};

/**
 * Loads a resource using http GET
 */
HttpClient.prototype.load = function(url, options) {
  options = options || {};
  var done = options.done || noop;
  var self = this;
  var deferred = Q.defer();

  var headers = options.headers || this.config.headers;
  var accept = options.accept || headers.Accept || this.config.headers.Accept;

  var req = request
    .get(url)
    .set(headers)
    .set('Accept', accept)
    .query(options.data || {});

  req.end(end(self, done, deferred));
  return deferred.promise;
};

/**
 * Performs a PUT HTTP request
 */
HttpClient.prototype.put = function(path, options) {
  options = options || {};
  var done = options.done || noop;
  var self = this;
  var deferred = Q.defer();
  var url = this.config.baseUrl + (path ? '/' + path : '');

  var headers = options.headers || this.config.headers;
  headers.Accept = headers.Accept || this.config.headers.Accept;

  var req = request
    .put(url)
    .set(headers)
    .send(options.data || {});

  req.end(end(self, done, deferred));
  return deferred.promise;
};

/**
 * Performs a DELETE HTTP request
 */
HttpClient.prototype.del = function(path, options) {
  options = options || {};
  var done = options.done || noop;
  var self = this;
  var deferred = Q.defer();
  var url = this.config.baseUrl + (path ? '/' + path : '');

  var headers = options.headers || this.config.headers;
  headers.Accept = headers.Accept || this.config.headers.Accept;

  var req = request
    .del(url)
    .set(headers)
    .send(options.data || {});

  req.end(end(self, done, deferred));
  return deferred.promise;
};

/**
 * Performs a OPTIONS HTTP request
 */
HttpClient.prototype.options = function(path, options) {
  options = options || {};
  var done = options.done || noop;
  var self = this;
  var deferred = Q.defer();
  var url = this.config.baseUrl + (path ? '/' + path : '');

  var headers = options.headers || this.config.headers;
  headers.Accept = headers.Accept || this.config.headers.Accept;

  var req = request('OPTIONS', url).set(headers);

  req.end(end(self, done, deferred));
  return deferred.promise;
};

module.exports = HttpClient;
