'use strict';

var angular = require('angular'),
    dashboard = require('./dashboard'),
    // decision definition
    decisionInstanceTable = require('./decisionDefinition/decisionInstanceTable'),

    // decision instance
    inputTable = require('./decisionInstance/inputTable'),
    outputTable = require('./decisionInstance/outputTable'),

    // decision instance table
    highlightRules = require('./decisionInstance/highlightRules'),
    realInput = require('./decisionInstance/realInput'),
    realOutput = require('./decisionInstance/realOutput');

var ngModule = angular.module('cockpit.plugin.decisionList.views', [dashboard.name]);

ngModule.config(decisionInstanceTable);
ngModule.config(inputTable);
ngModule.config(outputTable);
ngModule.config(highlightRules);
ngModule.config(realInput);
ngModule.config(realOutput);

module.exports = ngModule;
