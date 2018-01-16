'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    routeUtil = require('./../../../../common/scripts/services/routeUtil'),
    page = require('./../../../../common/scripts/services/page'),
    camAPI = require('./../../../../common/scripts/services/cam-api'),
    localConf = require('camunda-commons-ui/lib/services/cam-local-configuration');

var servicesModule = module.exports = angular.module('cam.admin.services', []);

servicesModule.service('page', page);
servicesModule.factory('routeUtil', routeUtil);
servicesModule.factory('camAPI', camAPI);
servicesModule.factory('localConf', localConf);
