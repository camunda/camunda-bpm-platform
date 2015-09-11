'use strict';

var DecisionDefinitionPage = require('./instance-view');
var TablePage = require('./../dmn-table');
var InputsTab = require('./tabs/inputs-tab.js');
var OutputsTab = require('./tabs/outputs-tab.js');

module.exports = new DecisionDefinitionPage();
module.exports.table = new TablePage();
module.exports.inputsTab = new InputsTab();
module.exports.outputsTab = new OutputsTab();
