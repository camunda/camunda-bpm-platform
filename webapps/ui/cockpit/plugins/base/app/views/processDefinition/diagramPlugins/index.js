'use strict';

var angular = require('angular');
var camCommon = require('cam-common');

var instanceCount = require('./instanceCount');

var ngModule = angular.module('cockpit.plugin.base.views.definition.diagram-plugins', [
  camCommon.name
]);

ngModule.config(instanceCount);

module.exports = ngModule;
