'use strict';

var WelcomePage =       require('./welcome-view');
var AuthenticationPage =  require('../../../../common/tests/pages/authentication');

module.exports = new WelcomePage();
module.exports.authentication = new AuthenticationPage();
