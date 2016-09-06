'use strict';

var DecisionDefinitionPage = require('./definition-view');
var TableDecisionInstancesPage = require('./tabs/decision-instances-tab');
var TablePage = require('./../dmn-table');
var VersionPage = require('./version');
var InformationPage = require('./../sidebar-information');
var SearchWidget = require('./../../../../common/tests/pages/search-widget');

module.exports = new DecisionDefinitionPage();
module.exports.table = new TablePage();
module.exports.version = new VersionPage();
module.exports.decisionInstancesTab = new TableDecisionInstancesPage();
module.exports.information = new InformationPage();
module.exports.search = new SearchWidget();
