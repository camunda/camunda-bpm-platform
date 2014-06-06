/* global ngDefine: false */
/**
  Defines a filter to convert given milliseconds into
  human readable time lapse.

  @name duration
  @memberof cam.cockpit.filter
  @type angular.filter

  @author Roman Smirnov <roman.smirnov@camunda.com>
 */
ngDefine('cockpit.filters.duration', function(module) {
  'use strict';

  var Filter = function() {
    return function(duration) {

      if (!duration) {
        return;
      }

      var x = duration / 1000;
      var seconds = Math.floor(x % 60);
      x /= 60;
      var minutes = Math.floor(x % 60);
      x /= 60;
      var hours = Math.floor(x % 24);
      x /= 24;
      var days = Math.floor(x);

      var result = [];

      addValue(days, 'day', result);
      addValue(hours, 'hour', result);
      addValue(minutes, 'minute', result);
      addValue(seconds, 'second', result);

      return result.join(', ');
      
    };

    function addValue(value, unit, array) {
      if (value) {
        var result = [];
        result.push(value);
        result.push(value === 1 ? unit : unit + 's');
        array.push(result.join(' '));
      }
    }

  };

  module.filter('duration', Filter);
});