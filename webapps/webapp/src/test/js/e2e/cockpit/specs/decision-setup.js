'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;

var deployFirst = operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups.dmn').toString()
  }]
}]);

var deploySecond = operation('deployment', 'create', [{
  deploymentName: 'assign-approver',
  files: [{
    name: 'assign-approver-groups-changed.dmn',
    content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups-changed.dmn').toString()
  }]
}]);


module.exports = {

  setup1: deployFirst,
  setup2: combine(deployFirst, deploySecond)

};
