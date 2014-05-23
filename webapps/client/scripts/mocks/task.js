'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'uuid', 'fixturer', 'underscore', 'jquery', 'camunda-tasklist/task/data'
], function(angular,   uuid,   fix,        _,            $) {


  /**
   * @module cam.tasklist.mocks.task
   */

  /**
   * @memberof cam.tasklist.mocks
   */

  var mockedModule = angular.module('cam.tasklist.task.data');


  var count = 400;
  var _mockedTasks = {};
  var _processes = [];

  var c = 1000;
  var now = Math.round((new Date()).getTime() / 1000);
  var day = 60 * 60 * 24;

  var _history = [
    {
      timestamp: now - (day * 42),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'something',
      orgValue: null,
      newValue: true
    },
    {
      timestamp: now - (day * 40),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'something',
      orgValue: null,
      newValue: true
    },
    {
      timestamp: now - (day * 32),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'fieldA',
      orgValue: null,
      newValue: true
    },
    {
      timestamp: now - (day * 8),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'fieldA',
      orgValue: true,
      newValue: false
    },
    {
      timestamp: now - (60 * 60 * 12),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'fieldB',
      orgValue: null,
      newValue: 'bla bla'
    },
    {
      timestamp: now - (60 * 60 * 8),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'fieldB',
      orgValue: null,
      newValue: 'bla bla'
    },
    {
      timestamp: now - (60 * 60 * 7),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'fieldB',
      orgValue: null,
      newValue: 'bla bla'
    },
    {
      timestamp: now - (60 * 60 * 7),
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'fieldB',
      orgValue: null,
      newValue: 'bla bla'
    },
    {
      timestamp: now - (60 * 60 * 7) + 60,
      userId: '',
      operationType: 'Claim',
      entityType: 'Task',
      property: 'fieldB',
      orgValue: null,
      newValue: 'bla bla'
    }
  ];

  var _fields = [
    {
      name: 'fieldA',
      label: 'A text field',
      placeholder: 'Some placeholder text',
      type: 'text',
      required: true,
      validate: /banane/i
    },
    {
      name: 'fieldB',
      label: 'An email',
      placeholder: 'max@mustermann.biz',
      type: 'email',
      required: true
    },
    {
      name: 'fieldC',
      label: 'A set of radios',
      type: 'radios',
      // validate:
      choices: [
        {
          value: 'a',
          label: 'Gimme a "A"'
        },
        {
          value: 'b',
          label: 'Gimme a "B"'
        },
        {
          value: 'c',
          label: 'Gimme a "C"'
        },
        {
          value: 'd',
          label: 'Gimme a "D"'
        }
      ]
    },
    {
      name: 'fieldD',
      label: 'A set of checkboxes',
      type: 'checkboxes',
      choices: [
        {
          value: 'a',
          label: 'Gimme a "A"'
        },
        {
          value: 'b',
          label: 'Gimme a "B"'
        }
      ]
    },
    {
      name: 'fieldE',
      label: 'One checkbox',
      type: 'checkbox',
      value: 'ok'
    }
  ];



  for (var p = 0; p < 10; p++) {
    var processId = uuid();
    var key = fix.thingName(4).split(/[^a-z0-9]/ig).join('-').toLowerCase();
    var _tasks = [];
    var _instances = [];
    var _processTasks = [];

    for (var pi = 0; pi < 10; pi++) {
      _instances.push({
        // String  The id of the process instance.
        id:            uuid(),
        // String  The id of the process definition this instance belongs to.
        definitionId:  processId,
        // String  The business key of the process instance.
        businessKey:   fix.randomString(),
        // Boolean  A flag indicating whether the process instance has ended. Deprecated: will always be false!
        ended:         false,
        // Boolean A flag indicating whether the process instance is suspended.
        suspended:     !!fix.random(0, 1)
      });
    }

    for (var t = 0; t < 10; t++) {
      _processTasks.push({
        id:            uuid(),
        name:          fix.thingName(3)
      });
    }

    _processes.push({
      // String  The id of the process definition.
      id:             processId,
      // String  The key of the process definition, i.e. the id of the BPMN 2.0 XML process definition.
      key:            key,
      // String  The category of the process definition.
      category:       fix.thingName(1),
      // String  The description of the process definition.
      description:    fix.loremIpsum(10),
      // String  The name of the process definition.
      name:           fix.thingName(fix.random(1, 4)),
      // Number  The version of the process definition that the engine assigned to it.
      version:        fix.random(),
      // String  The file name of the process definition.
      resource:       key +'.bpmn',
      // String  The id of the process definition.
      deploymentId:   uuid(),
      // String  The file name of the process definition diagram, if exists.
      diagram:        null,
      // Boolean A flag indicating whether the definition is suspended.
      suspended:      !!fix.random(0, 1),


      _instances:     _instances,
      _processTasks:  _processTasks
    });
  }

  for (var i = 0; i < count; i++) {
    var _process = fix.randomItem(_processes);
    var _task = fix.randomItem(_process._processTasks);

    _mockedTasks[_task.id] = {
      // The id of the task.
      id:                     _task.id,
      // The tasks name.
      name:                   _task.name,
      // The user assigned to this task.
      assignee:               (fix.random(0, 1) ? fix.personName() : (fix.random(0, 1) ? "Max Mustermann" : "")),
      // The time the task was created. Format yyyy-MM-dd'T'HH:mm:ss.
      created:                fix.randomDate(new Date(2014, 2, 1), new Date(2014, 6, 12)),
      // The due date for the task. Format yyyy-MM-dd'T'HH:mm:ss.
      due:                    fix.randomDate(new Date(2014, 2, 1), new Date(2014, 6, 12)),
      // The follow-up date for the task. Format yyyy-MM-dd'T'HH:mm:ss.
      followUp:               fix.randomDate(new Date(2014, 2, 1), new Date(2014, 6, 12)),
      // The delegation state of the task. Corresponds to the DelegationState enum in the engine. Possible values are RESOLVED and PENDING.
      delegationState:        fix.randomItem(['RESOLVED', 'PENDING']),
      // The task description.
      description:            fix.loremIpsum(30),
      // The id of the execution the task belongs to.
      executionId:            fix.randomString(12),
      // The owner of the task.
      owner:                  (fix.random(0, 1) ? fix.personName() : (fix.random(0, 1) ? 'Max Mustermann' : '')),
      // The id of the parent task, if this task is a subtask.
      parentTaskId:           fix.randomString(12),
      // The priority of the task.
      priority:               fix.random(0, 100),
      // The id of the process definition this task belongs to.
      processDefinitionId:    _process.id,
      // The id of the process instance this task belongs to.
      processInstanceId:      fix.randomItem(_process._instances, 1),
      // The task definition key.
      taskDefinitionKey:      _process.key,

      // NOT IMPLEMENTED YET
      processDefinitionName:  _process.name
    };
  }

  // sexyness: https://github.com/appendto/jquery-mockjax
  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: '/tasklist/tasks',
    response: function() {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/tasks'
          }
        },
        _embedded: {
          tasks: _.toArray(_mockedTasks)
        }
      };

      this.responseText = JSON.stringify(hal);
    }
  });

  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: /\/tasklist\/tasks\/([0-9a-z-]+)$/g,
    data: ['taskId'],
    response: function(settings) {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/tasks/'+ settings.taskId
          }
        }
      };

      _.extend(hal, _mockedTasks[settings.taskId]);

      this.responseText = JSON.stringify(hal);
    }
  });

  return mockedModule;
});
