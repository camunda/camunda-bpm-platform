
var GroupsPage = require('./groups');
var EditGroupPage = require('./editGroup');
var NewGroupPage = require('./newGroup');

module.exports = new GroupsPage();
module.exports.editGroup = new EditGroupPage();
module.exports.newGroup = new NewGroupPage();
