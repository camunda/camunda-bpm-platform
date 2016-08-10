'use strict';
/* jshint browserify: true */
var angular = require('camunda-commons-ui/vendor/angular'),
    welcome = require('./welcome');

var pagesModule = angular.module('cam.welcome.pages', []);

pagesModule.config(welcome);

module.exports = pagesModule;
