'use strict';

var GroupsPage = require('./groups-dashboard');
var EditGroupPage = require('./edit');
var NewGroupPage = require('./new');

module.exports = new GroupsPage();
module.exports.editGroup = new EditGroupPage();
module.exports.newGroup = new NewGroupPage();
