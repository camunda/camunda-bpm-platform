'use strict';

var SystemPage = require('./system-base');
var GeneralPage = require('./general');
var LicenseKeyPage = require('./license-key');

module.exports = new SystemPage();
module.exports.general = new GeneralPage();
module.exports.licenseKey = new LicenseKeyPage();
