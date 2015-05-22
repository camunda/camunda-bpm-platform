'use strict';

var SystemPage = require('./system-base');
var GeneralPage = require('./general');
var LicenseKeyPage = require('./license-key');
var FlowNodeCountPage = require('./flow-node-count');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new SystemPage();
module.exports.general = new GeneralPage();
module.exports.licenseKey = new LicenseKeyPage();
module.exports.authentication = new AuthenticationPage();
module.exports.flowNodeCount = new FlowNodeCountPage();

