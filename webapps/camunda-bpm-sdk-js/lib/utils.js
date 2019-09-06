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

/**
 * @exports CamSDK.utils
 */
var utils = (module.exports = {typeUtils: require('./forms/type-util')});

utils.solveHALEmbedded = function(results) {
  function isId(str) {
    if (str.slice(-2) !== 'Id') {
      return false;
    }

    var prop = str.slice(0, -2);
    var embedded = results._embedded;
    return !!(embedded[prop] && !!embedded[prop].length);
  }

  function keys(obj) {
    var arr = Object.keys(obj);

    for (var a in arr) {
      if (arr[a][0] === '_' || !isId(arr[a])) {
        arr.splice(a, 1);
      }
    }

    return arr;
  }

  var _embeddedRessources = Object.keys(results._embedded || {});
  for (var r in _embeddedRessources) {
    var name = _embeddedRessources[r];

    for (var i in results._embedded[name]) {
      results._embedded[name][i]._embedded =
        results._embedded[name][i]._embedded || {};

      var properties = keys(results._embedded[name][i]);

      for (var p in properties) {
        var prop = properties[p];
        if (results._embedded[name][i][prop]) {
          var embedded = results._embedded[prop.slice(0, -2)];
          for (var e in embedded) {
            if (embedded[e].id === results._embedded[name][i][prop]) {
              results._embedded[name][i]._embedded[prop.slice(0, -2)] = [
                embedded[e]
              ];
            }
          }
        }
      }
    }
  }

  return results;
};

// the 2 folowing functions were borrowed from async.js
// https://github.com/caolan/async/blob/master/lib/async.js

function _eachSeries(arr, iterator, callback) {
  callback = callback || function() {};
  if (!arr.length) {
    return callback();
  }
  var completed = 0;
  var iterate = function() {
    iterator(arr[completed], function(err) {
      if (err) {
        callback(err);
        callback = function() {};
      } else {
        completed += 1;
        if (completed >= arr.length) {
          callback();
        } else {
          iterate();
        }
      }
    });
  };
  iterate();
}

/**
 * Executes functions in serie
 *
 * @param  {(Object.<String, Function>|Array.<Function>)} tasks object or array of functions
 *                                                              taking a callback
 *
 * @param  {Function} callback                                  executed at the end, first argument
 *                                                              will be an error (if error occured),
 *                                                              the second depends on "tasks" type
 *
 * @example
 * CamSDK.utils.series({
 *   a: function(cb) { setTimeout(function() { cb(null, 1); }, 1); },
 *   b: function(cb) { setTimeout(function() { cb(new Error('Bang!')); }, 1); },
 *   c: function(cb) { setTimeout(function() { cb(null, 3); }, 1); }
 * }, function(err, result) {
 *   // err will be passed
 *   // result will be { a: 1, b: undefined }
 * });
 */
utils.series = function(tasks, callback) {
  callback = callback || function() {};

  var results = {};
  _eachSeries(
    Object.keys(tasks),
    function(k, callback) {
      tasks[k](function(err) {
        var args = Array.prototype.slice.call(arguments, 1);
        if (args.length <= 1) {
          args = args[0];
        }
        results[k] = args;
        callback(err);
      });
    },
    function(err) {
      callback(err, results);
    }
  );
};

/**
 * Escapes url string
 *
 * @param {string} string
 * @returns {string}
 */
utils.escapeUrl = function(string) {
  return encodeURIComponent(string)
    .replace(/\//g, '%2F')
    .replace(/%2F/g, '%252F')
    .replace(/\*/g, '%2A')
    .replace(/%5C/g, '%255C');
};
