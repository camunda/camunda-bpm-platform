'use strict';

var angular = require('angular');
var camCommon = require('cam-common');

var instanceCount = require('./instanceCount');
var callActivity = require('./callActivity');

var ngModule = angular.module('cockpit.plugin.base.views.instance.diagram-plugins', [
  camCommon.name
]);

ngModule.config(instanceCount);
ngModule.config(callActivity);

module.exports = ngModule;
