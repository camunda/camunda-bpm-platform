'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    userProfilePlugin = require('./profile/user-profile');

var pluginModule = angular.module('cam.welcome.plugins', []);

/* front-end only plugins */
pluginModule.config(userProfilePlugin);

module.exports = pluginModule;
