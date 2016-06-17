'use strict';

var angular = require('angular'),

    // dashboard
    decisionList = require('./dashboard/decision-list'),

    // decision definition
    decisionInstanceTable = require('./decisionDefinition/decisionInstanceTable'),

    // decision instance
    inputTable = require('./decisionInstance/inputTable'),
    outputTable = require('./decisionInstance/outputTable'),
    gotoProcessInstanceAction = require('./decisionInstance/gotoProcessInstanceAction'),

    // decision instance table
    highlightRules = require('./decisionInstance/highlightRules'),
    realInput = require('./decisionInstance/realInput'),
    realOutput = require('./decisionInstance/realOutput');

var ngModule = angular.module('cockpit.plugin.decisionList.views', []);

ngModule.config(decisionList);
ngModule.config(decisionInstanceTable);
ngModule.config(inputTable);
ngModule.config(outputTable);
ngModule.config(highlightRules);
ngModule.config(realInput);
ngModule.config(realOutput);

gotoProcessInstanceAction(ngModule);

module.exports = ngModule;
