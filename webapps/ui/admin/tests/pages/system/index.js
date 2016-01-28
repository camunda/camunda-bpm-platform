'use strict';

var SystemPage = require('./system-base');
var GeneralPage = require('./general');
var LicenseKeyPage = require('./license-key');
var MetricsPage = require('./execution-metrics');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

module.exports = new SystemPage();
module.exports.general = new GeneralPage();
module.exports.licenseKey = new LicenseKeyPage();
module.exports.authentication = new AuthenticationPage();
module.exports.executionMetrics = new MetricsPage();

