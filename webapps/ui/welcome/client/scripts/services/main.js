'use strict';

var angular = require('camunda-commons-ui/vendor/angular');
var camAPI = require('./../../../../common/scripts/services/cam-api');
var customLinks = require('./custom-links');

var servicesModule = angular.module('cam.welcome.services', []);

servicesModule.factory('camAPI', camAPI);
servicesModule.factory('customLinks', customLinks);

module.exports = servicesModule;
