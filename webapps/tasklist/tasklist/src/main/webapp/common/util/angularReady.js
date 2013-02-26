/**
 * Angular ready plugin for require.js
 */

define(["domReady"], function(domReady) {
  'use strict';

  var isBrowser = typeof window !== "undefined" && window.document,
      doc = isBrowser ? document : null,
      readyCalls = [],
      angularLoaded = false,
      loading = [];

  function isReady() {
    return angularLoaded && loading.length == 0;
  }

  function runCallbacks(callbacks) {
    var i;
    for (i = 0; i < callbacks.length; i += 1) {
      callbacks[i](doc);
    }
  }

  function callReady() {
    var callbacks = readyCalls;

    if (isReady()) {
      //Call the DOM ready callbacks
      if (callbacks.length) {
        readyCalls = [];
        runCallbacks(callbacks);
      }
    }
  }

  /** START OF PUBLIC API **/

  /**
   * Registers a callback for angular ready. If it is already ready, the
   * callback is called immediately.
   *
   * @param {Function} callback
   */
  function angularReady(callback) {
    if (isReady()) {
      callback(doc);
    } else {
      readyCalls.push(callback);
    }
    return angularReady;
  }

  angularReady.version = '0.0.1';

  /**
   * Loader Plugin API method
   */
  angularReady.load = function(name, req, onLoad, config) {
    angularReady(onLoad);
  };

  angularReady.loading = function(module) {
    angularLoaded = true;
    loading.push(module);
  };

  angularReady.loaded = function(module) {
    var idx = loading.indexOf(module);
    if (idx != -1) {
      // remove loaded stuff from loading
      loading.splice(idx, 1);
    }

    callReady();
  };

  /** END OF PUBLIC API **/

  return angularReady;
});