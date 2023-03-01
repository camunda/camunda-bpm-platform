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

// var HttpClient = require('./http-client');
var Q = require('q');
var Events = require('./../events');
var BaseClass = require('./../base-class');

/**
 * No-Op callback
 */
function noop() {}

/**
 * Abstract class for resources
 *
 * @class
 * @augments {CamSDK.BaseClass}
 * @memberof CamSDK.client
 *
 * @borrows CamSDK.Events.on                        as on
 * @borrows CamSDK.Events.once                      as once
 * @borrows CamSDK.Events.off                       as off
 * @borrows CamSDK.Events.trigger                   as trigger
 *
 * @borrows CamSDK.Events.on                        as prototype.on
 * @borrows CamSDK.Events.once                      as prototype.once
 * @borrows CamSDK.Events.off                       as prototype.off
 * @borrows CamSDK.Events.trigger                   as prototype.trigger
 *
 *
 * @example
 *
 * // create a resource Model
 * var Model = AbstractClientResource.extend({
 *   apiUri: 'path-to-the-endpoint'
 *   doSomethingOnInstance: function() {
 *     //
 *   }
 * }, {
 *   somethingStatic: {}
 * });
 *
 * // use the generated Model statically
 * // with events
 * Model.on('eventname', function(results) {
 *   // You probably have something like
 *   var total = results.count;
 *   var instances = results.items;
 * });
 * Model.list({ nameLike: '%call%' });
 *
 * // or alternatively by using a callback
 * Model.list({ nameLike: '%call%' }, function(err, results) {
 *   if (err) {
 *     throw err;
 *   }
 *
 *   var total = results.count;
 *   var instances = results.items;
 * });
 *
 * var instance = new Model();
 * instance.claim(function(err, result) {
 *
 * });
 */
var AbstractClientResource = BaseClass.extend(
  /** @lends AbstractClientResource.prototype */
  {
    /**
     * Initializes a AbstractClientResource instance
     *
     * This method is aimed to be overriden by other implementations
     * of the AbstractClientResource.
     *
     * @method initialize
     */
    initialize: function() {
      // do something to initialize the instance
      // like copying the Model http property to the "this" (instanciated)
      this.http = this.constructor.http;
    }
  },

  /** @lends AbstractClientResource */
  {
    /**
     * Path used by the resource to perform HTTP queries
     *
     * @abstract
     * @memberOf CamSDK.client.AbstractClientResource
     */
    path: '',

    /**
     * Object hosting the methods for HTTP queries.
     *
     * @abstract
     * @memberof CamSDK.client.AbstractClientResource
     */
    http: {},

    /**
     * Create an instance on the backend
     *
     * @abstract
     * @memberOf CamSDK.client.AbstractClientResource
     *
     * @param  {!Object|Object[]}  attributes
     * @param  {requestCallback} [done]
     */
    create: function() {},

    /**
     * Fetch a list of instances
     *
     * @memberof CamSDK.client.AbstractClientResource
     *
     * @fires CamSDK.AbstractClientResource#error
     * @fires CamSDK.AbstractClientResource#loaded
     *
     * @param  {?Object.<String, String>} params
     * @param  {requestCallback} [done]
     */
    list: function(params, done) {
      // allows to pass only a callback
      if (typeof params === 'function') {
        done = params;
        params = {};
      }
      params = params || {};
      done = done || noop;

      // var likeExp = /Like$/;
      var self = this;
      var results = {
        count: 0,
        items: []
      };

      var combinedPromise = Q.defer();

      var countFinished = false;
      var listFinished = false;

      var checkCompletion = function() {
        if (listFinished && countFinished) {
          self.trigger('loaded', results);
          combinedPromise.resolve(results);
          done(null, results);
        }
      };

      // until a new webservice is made available,
      // we need to perform 2 requests.
      // Since they are independent requests, make them asynchronously
      self.count(params, function(err, count) {
        if (err) {
          self.trigger('error', err);
          combinedPromise.reject(err);
          done(err);
        } else {
          results.count = count;
          countFinished = true;
          checkCompletion();
        }
      });

      self.http.get(self.path, {
        data: params,
        done: function(err, itemsRes) {
          if (err) {
            self.trigger('error', err);
            combinedPromise.reject(err);
            done(err);
          } else {
            results.items = itemsRes;
            // QUESTION: should we return that too?
            results.firstResult = parseInt(params.firstResult || 0, 10);
            results.maxResults =
              results.firstResult + parseInt(params.maxResults || 10, 10);
            listFinished = true;
            checkCompletion();
          }
        }
      });

      return combinedPromise.promise;
    },

    /**
     * Fetch a count of instances
     *
     * @memberof CamSDK.client.AbstractClientResource
     *
     * @fires CamSDK.AbstractClientResource#error
     * @fires CamSDK.AbstractClientResource#loaded
     *
     * @param  {?Object.<String, String>} params
     * @param  {requestCallback} [done]
     */
    count: function(params, done) {
      // allows to pass only a callback
      if (typeof params === 'function') {
        done = params;
        params = {};
      }
      params = params || {};
      done = done || noop;
      var self = this;
      var deferred = Q.defer();

      this.http.get(this.path + '/count', {
        data: params,
        done: function(err, result) {
          if (err) {
            /**
             * @event CamSDK.AbstractClientResource#error
             * @type {Error}
             */
            self.trigger('error', err);

            deferred.reject(err);
            done(err);
          } else {
            deferred.resolve(result.count);
            done(null, result.count);
          }
        }
      });

      return deferred.promise;
    },

    /**
     * Update one or more instances
     *
     * @abstract
     * @memberof CamSDK.AbstractClientResource
     *
     * @param  {!String|String[]}     ids
     * @param  {Object.<String, *>}   attributes
     * @param  {requestCallback} [done]
     */
    update: function() {},

    /**
     * Delete one or more instances
     *
     * @abstract
     * @memberof CamSDK.AbstractClientResource
     *
     * @param  {!String|String[]}  ids
     * @param  {requestCallback} [done]
     */
    delete: function() {}
  }
);

Events.attach(AbstractClientResource);

module.exports = AbstractClientResource;
