'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'uuid', 'fixturer', 'underscore', 'jquery', 'camunda-tasklist/process/data'
], function(angular,   uuid,   fixturer,   _,            $) {

  var mockedModule = angular.module('cam.tasklist.process.data');

  var _mockedProcesses = {};
  var id;

  for (var i = 0; i < 120; i++) {
    id = uuid();
    _mockedProcesses[id] = fixturer({
      // String  The id of the process definition.
      'id':           id,
      // String  The key of the process definition, i.e. the id of the BPMN 2.0 XML process definition.
      'key':          fixturer.randomString(20),
      // String  The category of the process definition.
      'category':     fixturer.thingName(2),
      // String  The description of the process definition.
      'description':  fixturer.loremIpsum(),
      // String  The name of the process definition.
      'name':         fixturer.thingName(2),
      // Number  The version of the process definition that the engine assigned to it.
      'version':      fixturer.random(3, 9),
      // String  The file name of the process definition.
      'resource':     fixturer.randomString(20) +'.bpmn',
      // String  The id of the process definition.
      'deploymentId': uuid(),
      // String  The file name of the process definition diagram, if exists.
      'diagram':      false,
      // Boolean A flag indicating whether the definition is suspended.
      'suspended':    !!fixturer.random(0, 1)
    });
  }

  function idFromKey(key) {
    for (var i in _mockedProcesses) {
      if (_mockedProcesses[i].key === key) {
        return i;
      }
    }
  }



  // not when integrated into camunda-bpm-platform
  if ($('base').attr('href') === '/') {
    $.mockjax({
      method: 'GET',
      contentType: 'application/json',
      url: '/camunda/api/engine/engine/default/process-definition',
      response: function() {
        var arr = _.toArray(_mockedProcesses).slice(0, 20);
        this.responseText = arr;
      }
    });

    $.mockjax({
      method: 'GET',
      contentType: 'application/json',
      url: '/camunda/api/engine/engine/default/process-definition/count',
      response: function() {
        var arr = _.toArray(_mockedProcesses);
        this.responseText = {count: arr.length};
      }
    });

    $.mockjax({
      method: 'POST',
      contentType: 'application/json',
      url: /\/camunda\/api\/engine\/engine\/default\/process-definition\/key\/([a-z0-9-_]+)\/start/i,
      urlParams: ['key'],
      // http://docs.camunda.org/latest/api-references/rest/#process-definition-start-process-instance-result
      response: function(settings) {
        this.responseText = {
          // String  The id of the process instance.
          id: uuid(),
          // String  The id of the process definition.
          definitionId: idFromKey(settings.urlParams.key),
          // String  The business key of the process instance.
          businessKey: fixturer.randomString(20),
          // Boolean A flag indicating whether the instance is still running.
          ended: false,
          // Boolean A flag indicating whether the instance is suspended.
          suspended: false,
          // Object  A json array containing links to interact with the instance.
          links: {}
        };
      }
    });
  }





  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: '/tasklist/processes',
    response: function() {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/processes'
          }
        },
        _embedded: {
          processes: _.toArray(_mockedProcesses)
        }
      };

      this.responseText = JSON.stringify(hal);
    }
  });

  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: /\/tasklist\/processes\/([0-9a-z-]+)$/g,
    data: ['processId'],
    response: function(settings) {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/processes/'+ settings.processId
          }
        }
      };

      _.extend(hal, _mockedProcesses[settings.processId]);

      this.responseText = JSON.stringify(hal);
    }
  });

  return mockedModule;
});
