'use strict';

var angular = require('angular'),
    base = require('./base/app/plugin'),
    decisionList = require('./decisionList/app/plugin'),
    jobDefinition = require('./jobDefinition/app/plugin'),
    tasks = require('./tasks/app/plugin');

module.exports = angular.module('cockpit.plugin.cockpitPlugins', [
  base.name,
  decisionList.name,
  jobDefinition.name,
  tasks.name
]);
