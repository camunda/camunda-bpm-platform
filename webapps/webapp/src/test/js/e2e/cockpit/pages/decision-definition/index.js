'use strict';

var DecisionDefinitionPage = require('./definition-view');
var TablePage = require('./../dmn-table');
var VersionPage = require('./version');

module.exports = new DecisionDefinitionPage();
module.exports.table = new TablePage();
module.exports.version = new VersionPage();
