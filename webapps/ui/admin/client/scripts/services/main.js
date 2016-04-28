'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    routeUtil = require('./../../../../common/scripts/services/routeUtil'),
    page = require('./../../../../common/scripts/services/page'),
    camAPI = require('./../../../../common/scripts/services/cam-api');

var servicesModule = module.exports = angular.module('cam.admin.services', []);

servicesModule.service('page', page);
servicesModule.factory('routeUtil', routeUtil);
servicesModule.factory('camAPI', camAPI);
