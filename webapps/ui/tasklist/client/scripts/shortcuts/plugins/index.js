'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    plugin = require('./action/cam-tasklist-shortcut-help-plugin');

var shortcutModule = angular.module('cam.tasklist.shortcuts', []);

/* action plugins */
shortcutModule.config(plugin);

module.exports = shortcutModule;
