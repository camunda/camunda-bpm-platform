"use strict";

define(["angular"], function(angular) {

  var module = angular.module("common.services");

  var DebouncerFactory = function($timeout) {

    var timer;

    /**
     * Debounce a function call, making it callable an arbitrary number of times before it is actually executed once.
     *
     * @param func {function()} The function to debounce.
     * @param wait {number} The debounce timeout.
     * @return {function()} A function that can be called an arbitrary number
     *         of times within the given time.
     */
    return function debounce(func, wait) {
      return function() {
        var context = this,
            args = arguments;

        if (timer) {
          $timeout.cancel(timer);
        }

        timer = $timeout(function() {
          timer = null;
          func.apply(context, args);
        }, wait);
      };
    };
  };

  DebouncerFactory.$inject = [ "$timeout" ];

  module.factory("debounce", DebouncerFactory);
});
