'use strict';

var angular = require('angular');
var navbarModule = require('./navbar/main');

module.exports = angular.module('tasklist.plugin.standaloneTask', [navbarModule.name]);
