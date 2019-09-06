/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';


/**
 * Console proxy
 * to see the logs
 * - in browser, from the dev tools console: localStorage.setItem('camsdklog', true);
 * - in node, set an environment variable, like CAMSDKLOG=1
 */
function log() {
  var b = typeof window !== 'undefined' &&
          window.localStorage &&
          window.localStorage.getItem('camsdklog');

  var n = typeof process !== 'undefined' &&
          process.env.CAMSDKLOG;

  if (b || n) {
    console.info.apply(console, arguments);
  }
}



/**
 * Variable to store fixtures
 * @private
 * @type {Object}
 * @property {Object.<uuid, Object>} processDefinition  Process definition mocks
 * @property {Object.<uuid, Object>} processInstance    Process instance mocks
 * @property {Object.<uuid, Object>} task               Task mocks
 * @property {Object.<uuid, Object>} user               User mocks
 * @property {Object.<uuid, Object>} variable           Variable mocks
 */
var _store = {};
var fixturer = require('fixturer');
var uuid = require('uuid');
var _ = require('underscore');
_.str = require('underscore.string');

var i = 0, id;
_store.processDefinition = {};
for (; i < 80; i++) {
  id = uuid();
  _store.processDefinition[id] = {
    id: id,
    key: fixturer.randomString(fixturer.random(8, 16)),
    // name: fixturer.thingName(),
    name: fixturer.randomItem([
      'Call Process '+ i,
      'process '+ i,
      'yada '+ i,
      'foo Bar '+ i,
      'Foo-Bar '+ i
    ]),
    resource: fixturer.randomString(fixturer.random(4, 8)),
    diagram: fixturer.randomString(fixturer.random(4, 8)),
    version: fixturer.random(),
    deploymentId: uuid(),
    suspended: !!fixturer.random(0, 1),
    category: fixturer.randomItem([
      'category1',
      'category2',
      'category3',
      'category4',
      'category5'
    ], 1)
  };
}


_store.user = {};
for (i = 0; i < 40; i++) {
  id = fixturer.randomString(fixturer.random(4, 8));
  _store.user[id] = {
    id: id,
    firstName: fixturer.personName(true, false),
    lastName: fixturer.personName(false, true),
    email: fixturer.randomString(fixturer.random(4, 8)) +'@'+ fixturer.randomString(fixturer.random(5, 7)) +'.com'
  };
}
_store.user.jonny1 = {
  id: 'jonny1',
  firstName: 'Jonny',
  lastName: 'One',
  email: fixturer.randomString(fixturer.random(4, 8)) +'@'+ fixturer.randomString(fixturer.random(5, 7)) +'.com'
};



_store.processInstance = {};
for (var processDefinitionId in _store.processDefinition) {
  var count = fixturer.random(0, 80);
  for (i = 0; i < count; i++) {
    id = uuid();
    _store.processInstance[id] = {
      links: [],
      id: id,
      definitionId: processDefinitionId,
      businessKey: fixturer.randomString(fixturer.random(8, 16)),
      ended: !!fixturer.random(0, 1),
      suspended: !!fixturer.random(0, 1)
    };
  }
}

var users = _.keys(_store.user);
users.push(null);

var procInst, procInstId;
var procInstances = _.keys(_store.processInstance);
// procInstances.push(null);

_store.task = {};
for (i = 0; i < 400; i++) {
  id = uuid();
  procInstId = fixturer.randomItem(procInstances);
  procInst = _store.processInstance[procInstId];
  var created = fixturer.randomDate(new Date('2014-01-01'), new Date());

  _store.task[id] = {
    id: id,
    name: fixturer.thingName(fixturer.random(2, 12)),
    assignee: fixturer.randomItem(users),
    created: created,
    due: fixturer.randomDate(created, new Date('2014-11-29')),
    followUp: fixturer.randomDate(created, new Date('2014-10-29')),
    delegationState: 'RESOLVED',
    description: fixturer.thingName(fixturer.random(7, 42)),
    executionId: uuid(),
    owner: fixturer.randomItem(users),
    // should link to a existing task.id
    parentTaskId: null,
    priority: fixturer.random(),
    processDefinitionId: procInst.definitionId,
    processInstanceId: procInstId,
    taskDefinitionKey: 'Task_'+ fixturer.random(1, 20)
  };
}

var subId;
for (id in  _store.task) {
  if (fixturer.random(1, 6) > 3) {
    var created = fixturer.randomDate(new Date('2014-01-01'), new Date());
    subId = uuid();
    procInstId = _store.task[id].processInstanceId;
    procInst = _store.processInstance[procInstId];

    _store.task[subId] = {
      id: subId,
      name: fixturer.thingName(),
      assignee: fixturer.randomItem(users),
      created: created,
      due: fixturer.randomDate(created, new Date('2014-11-29')),
      followUp: fixturer.randomDate(created, new Date('2014-10-29')),
      delegationState: 'RESOLVED',
      description: fixturer.thingName(fixturer.random(7, 42)),
      executionId: uuid(),
      owner: fixturer.randomItem(users),
      // should link to a existing task.id
      parentTaskId: id,
      priority: fixturer.random(),
      processDefinitionId: procInst.definitionId,
      processInstanceId: procInstId,
      taskDefinitionKey: null
    };
  }
}



_store.variable = {};
for (i = 0; i < 600; i++) {
  id = uuid();
  _store.variable[id] = {
    id: id,
    name: fixturer.randomString(fixturer.random(4, 16)),
    type: fixturer.randomItem([
      'integer',
      'double',
      'string'
    ]),
    value: 5,
    processInstanceId: null,
    executionId: uuid(),
    taskId: null,
    activityInstanceId: 'Task_1:b68b71ca-e310-11e2-beb0-f0def1557726'
  };
}

_store.filter = {};
_.each([
  {
    id: uuid(),
    resourceType: 'task',
    name: 'No filter',
    owner: 'jonny1',
    query: [],
    properties: {
      variables: [
        {
          name: 'varA',
          label: 'Variable A'
        },
        {
          name: 'varB',
          label: 'Variable B'
        }
      ],
      description: 'All the things to be done.',
      color: '#AFB3E2'
    }
  },
  {
    id: uuid(),
    resourceType: 'task',
    name: 'My tasks',
    owner: 'jonny1',
    query: [
      {
        key: 'assignee',
        value: '{self}'
      }
    ],
    properties: {
      variables: [
        {
          name: 'varA',
          label: 'Variable A'
        },
        {
          name: 'varB',
          label: 'Variable B'
        }
      ],
      description: 'All the tasks who were assigned to me.',
      color: '#AFB3E2'
    }
  },
  {
    id: uuid(),
    resourceType: 'task',
    name: 'Overdue',
    owner: 'jonny1',
    query: [
      {
        key: 'dueBefore',
        // operator: 'smaller',
        value: '{now}'
      }
    ],
    properties: {
      variables: [
        {
          name: 'varA',
          label: 'Variable A'
        },
        {
          name: 'varB',
          label: 'Variable B'
        }
      ],
      description: 'Tasks who should already have been finished.',
      color: '#FFB4B4'
    }
  },
  {
    id: uuid(),
    resourceType: 'task',
    name: 'Due in 3 days',
    owner: 'jonny1',
    query: [
      {
        key: 'dueBefore',
        // operator: 'smaller',
        value: '{now} + ({day} * 3)'
      }
    ],
    properties: {
      variables: [
        {
          name: 'varA',
          label: 'Variable A'
        },
        {
          name: 'varB',
          label: 'Variable B'
        }
      ],
      description: '',
      color: '#FFD2D2'
    }
  },
  {
    id: uuid(),
    resourceType: 'task',
    name: 'Group A',
    owner: 'jonny1',
    query: [
      {
        key: 'candidateGroup',
        // operator: 'has',
        value: 'group-a'
      }
    ],
    properties: {
      variables: [
        {
          name: 'varA',
          label: 'Variable A'
        },
        {
          name: 'varB',
          label: 'Variable B'
        }
      ],
      description: '',
      color: ''
    }
  },
  {
    id: uuid(),
    resourceType: 'task',
    name: 'Group B',
    owner: 'jonny1',
    query: [
      {
        key: 'candidateGroup',
        // operator: 'has',
        value: 'group-a'
      }
    ],
    properties: {
      variables: [
        {
          name: 'varA',
          label: 'Variable A'
        },
        {
          name: 'varB',
          label: 'Variable B'
        }
      ],
      description: '',
      color: ''
    }
  }
], function(filter) {
  _store.filter[filter.id] = filter;
});


_store.processInstanceFormVariables = {};

_store.processDefinitionFormVariables = {};
for (id in _store.processDefinition) {
  _store.processDefinitionFormVariables[id] = {
    integerVar: {
      id: uuid(),
      name: 'integerVar',
      type: 'integer',
      value: fixturer.random(4, 16),
      processInstanceId: id,
      executionId: 'b68b71c9-e310-11e2-beb0-f0def1557726',
      taskId: null,
      activityInstanceId: 'Task_1:b68b71ca-e310-11e2-beb0-f0def1557726'
    },
    stringVar: {
      id: uuid(),
      name: 'stringVar',
      type: 'string',
      value: fixturer.randomString(fixturer.random(4, 16)),
      processInstanceId: id,
      executionId: 'b68b71c9-e310-11e2-beb0-f0def1557726',
      taskId: null,
      activityInstanceId: 'Task_1:b68b71ca-e310-11e2-beb0-f0def1557726'
    }
  };
}

function filter(src, data) {
  var where = {
    exact:  {},
    like:   {},
    before: {},
    after:  {}
  };
  var notFilters = [
    'sortBy',
    'sortOrder',
    'firstResult',
    'maxResults'
  ];

  var likeExp = /Like$/;
  var beforeExp = /Before$/;
  var afterExp = /After$/;

  _.each(data, function(val, key) {
    if (notFilters.indexOf(key) > -1) { return; }

    /* jshint evil: true */
    if (beforeExp.test(key)) {
      where.before[key.split(beforeExp).shift()] = new Date(eval(val) * 1000);
    }
    else if (afterExp.test(key)) {
      where.after[key.split(afterExp).shift()] = new Date(eval(val) * 1000);
    }
    /* jshint evil: false */
    else if (likeExp.test(key)) {
      where.like[key.split(likeExp).shift()] = new RegExp((''+ val).slice(1).slice(0, -1), 'g');
    }
    else {
      where.exact[key] = val;
    }
  });

  var found = _.size(where.exact) ? _.where(src, where.exact) : _.toArray(src);

  found = _.size(where.like) ? _.filter(found, function(item) {
    var keep = true;
    // var realKey;
    _.each(where.like, function(search, key) {
      if (!keep) { return; }
      keep = search.test(''+ item[key]);
    });
    return keep;
  }) : found;

  found = _.size(where.before) ? _.filter(found, function(item) {
    var keep = true;
    _.each(where.before, function(val, key) {
      if (!keep) { return; }
      keep = item[key] <= val;
    });
    return keep;
  }) : found;

  found = _.size(where.after) ? _.filter(found, function(item) {
    var keep = true;
    _.each(where.after, function(val, key) {
      if (!keep) { return; }
      keep = item[key] >= val;
    });
    return keep;
  }) : found;


  // just do some cleanup..
  for (var c in where) {
    if (!_.size(where[c])) {
      delete where[c];
    }
  }
  if (_.size(where)) {
    log(''+ found.length +' / '+ _.size(src) +' record(s) matching\n'+ JSON.stringify(where, null, 2));
  }
  else {
    log('No filter applied');
  }

  return found;
}


function genericGet(wanted, items, where) {
  var returned;

  if (wanted === 'key') {
    returned = _.findWhere(items, {key: wanted});
  }

  else if (!wanted || wanted === 'count') {
    returned = filter(items, where);

    // returns an object with "count"
    if (wanted) {
      returned = { count: returned.length };
    }
    // returns an aray of items
    else {
      var offset = parseInt(where.firstResult || 0, 10);
      var limit = offset + parseInt(where.maxResults || 10, 10);
      returned = _.toArray(returned).slice(offset, limit);
    }
  }

  // request by id
  else if (wanted) {
    returned = items[wanted];
  }

  return returned;
}



module.exports = [
  {
    /**
     * regular expression of URL
     */
    pattern: 'engine-rest/engine/engine/default/(.*)',

    /**
     * returns the data
     *
     * @param match array Result of the resolution of the regular expression
     * @param params object sent by 'send' function
     * @param headers object set by 'set' function
     */
    fixtures: function(match, params, headers) {
      return params;
    },

    /**
     * returns the result of the GET request
     *
     * @param match array Result of the resolution of the regular expression
     * @param data  mixed Data returns by `fixtures` attribute
     */
    get: function(match, fixture) {

      var pathParts = match[1].split('/');
      pathParts[pathParts.length - 1] = pathParts[pathParts.length - 1].split('?')[0];
      var resourceName = pathParts.shift();

      var data = {};
      if(match[1] && match[1].split('?')[1]) {
        var args = match[1].split('?')[1].split('&');
        for(var i = 0; i < args.length; i++) {
          var components = args[i].split('=');
          data[components[0]] = components[1];
        }
      }

      var results = {};

      switch (resourceName) {
      case 'process-definition':
        var action = pathParts[pathParts.length - 1];
        if (action === 'form-variables') {
          var definition;
          if (pathParts[0] === 'key') {
            definition = _.findWhere(_store.processDefinition, {key: pathParts[1]});
          }
          else {
            definition = _store.processDefinition[pathParts[0]];
          }
          results = _store.processDefinitionFormVariables[definition.id];
        }
        else {
          results = genericGet(pathParts[0], _store.processDefinition, data);
        }
        break;

      case 'process-instance':
        results = genericGet(pathParts[0], _store.processInstance, data);
        break;

      case 'authorization':
        results = genericGet(pathParts[0], _store.authorization, data);
        break;

      case 'filter':
        results = genericGet(pathParts[0], _store.filter, data);
        break;

        // case 'session':
        //   results = genericGet(pathParts[0], _store.session, data);
        //   break;

      case 'task':
        results = genericGet(pathParts[0], _store.task, data);
        break;

      case 'user':
        results = genericGet(pathParts[0], _store.user, data);
        break;

      case 'variable-instance':
        results = genericGet(pathParts[0], _store.variable, data);
        break;
      }

      //return results;

      return {
        body: results,
        ok: true
      };
    },

    /**
     * returns the result of the POST request
     *
     * @param match array Result of the resolution of the regular expression
     * @param data  mixed Data returns by `fixtures` attribute
     */
    post: function(match, data) {

      var pathParts = match[1].split('/');
      pathParts[pathParts.length - 1] = pathParts[pathParts.length - 1].split('?')[0];
      var resourceName = pathParts.shift();

      var urlData = {};
      if(match[1] && match[1].split('?')[1]) {
        var args = match[1].split('?')[1].split('&');
        for(var i = 0; i < args.length; i++) {
          var components = args[i].split('=');
          urlData[components[0]] = components[1];
        }
      }

      var results = {};

      switch (resourceName) {
      case 'process-definition':
        var action = pathParts[pathParts.length - 1];
        var definition;
        if (pathParts[0] === 'key') {
          definition = _.findWhere(_store.processDefinition, {key: pathParts[1]});
        }
        else {
          definition = _store.processDefinition[pathParts[0]];
        }


        switch (action) {
        case 'submit-form':
          var instanceId = uuid();
          var variables = data.variables;

          _store.processInstanceFormVariables[instanceId] = variables;

          _store.processInstance[instanceId] = {
            id: instanceId,
            definitionId: definition.id,
            businessKey: 'myBusinessKey',
            ended: false,
            suspended: false
          };

          results = _.extend(
            {
              links:[
                {
                  method: 'GET',
                  href: 'http://localhost:8080/rest-test/process-instance/'+ instanceId,
                  rel: 'self'
                }
              ],
            }, _store.processInstance[instanceId]);
          break;
        }
        break;
      }

      return {
        body: results,
        ok: true
      };
    }
  }
];
module.exports.mockedData = _store;
