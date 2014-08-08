'use strict';

var ProcessInstancePage = require('./instance-view');
var DiagramPage = require('./../diagram');

module.exports = new ProcessInstancePage();
module.exports.diagram = new DiagramPage();