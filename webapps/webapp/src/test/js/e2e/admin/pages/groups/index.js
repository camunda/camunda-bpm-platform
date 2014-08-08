'use strict';

var GroupsPage = require('./groups-dashboard');
var EditGroupPage = require('./edit');
var NewGroupPage = require('./new');
var AuthenticationPage = require('../../../commons/pages/authentication');

module.exports = new GroupsPage();
module.exports.editGroup = new EditGroupPage();
module.exports.newGroup = new NewGroupPage();
module.exports.authentication = new AuthenticationPage();