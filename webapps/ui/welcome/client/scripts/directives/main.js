'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    userProfile = require('./user-profile'),
    customLinks = require('./custom-links');

var directivesModule = angular.module('cam.welcome.directives', []);

directivesModule.directive('userProfile', userProfile);
directivesModule.directive('customLinks', customLinks);

module.exports = directivesModule;
