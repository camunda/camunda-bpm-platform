/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

var fs = require('fs');
var ops = module.exports = {};


// forms are located at:
// /webapp/src/main/webapp/develop/

// /webapp/src/test/js/e2e/tasklist/specs


var developResources = __dirname + '/../../../../../main/runtime/develop/resources/pa';


ops.deployment = {
  create: [{
    deploymentName:  'embedded-form',
    files:           [{
      name: 'user-tasks.bpmn',
      content: fs.readFileSync(developResources + '/invoice-embedded-forms2.bpmn').toString()
    }]
  },{
    deploymentName:  'generic-form',
    files:           [{
      name: 'process-with-sub-process.bpmn',
      content: fs.readFileSync(developResources + '/process-with-sub-process.bpmn').toString()
    }]
  // },
  // {
  //   deploymentName:  'generated-form',
  //   files:           [{
  //     name: 'generated-form.bpmn',
  //     content: fs.readFileSync(__dirname + '/../../resources/generated-form.bpmn').toString()
  //   }]
  }]
};
