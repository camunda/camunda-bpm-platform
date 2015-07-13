'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:

    combine(
      operation('task', 'create', [{
        id: '1',
        name: 'Task 1',
        assignee: 'test',
        due: '2016-09-15T15:45:48'
      },
      {
        id: '2',
        name: 'Task 2',
        assignee: 'test',
        due: '2016-09-16T15:45:48'
      },
      {
        id: 'abc123',
        name: 'My Task',
        assignee: 'test',
        due: '2016-09-15T15:46:48'
      }]),

      operation('task', 'localVariable', [{
        id: '1',
        varId: 'testVar',
        value: 42,
        type: 'Integer'
      }])

)};
