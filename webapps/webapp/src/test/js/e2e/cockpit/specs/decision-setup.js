'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;


module.exports = {

  setup1:

    operation('deployment', 'create', [{
      deploymentName: 'assign-approver',
      files: [{
        name: 'assign-approver-groups.dmn',
        content: fs.readFileSync(__dirname + '/../../resources/assign-approver-groups.dmn').toString()
      }]
    }])

};
