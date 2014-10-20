'use strict';

var ProcessInstancePage = require('./instance-view');
var DiagramPage = require('./../diagram');
var ActionButtonPage = require('./instance-action');

module.exports = new ProcessInstancePage();
module.exports.diagram = new DiagramPage();
module.exports.actionButton = new ActionButtonPage();