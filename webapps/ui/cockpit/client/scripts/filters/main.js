'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

    shorten = require('./shorten'),
    abbreviateNumber = require('camunda-commons-ui/lib/filter/abbreviateNumber'),
    duration = require('./duration');


var filtersModule = angular.module('cam.cockpit.filters', []);

filtersModule.filter('shorten', shorten);
filtersModule.filter('abbreviateNumber', abbreviateNumber);
filtersModule.filter('duration', duration);

module.exports = filtersModule;
