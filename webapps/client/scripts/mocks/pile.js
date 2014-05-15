'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'uuid', 'fixturer', 'underscore', 'jquery', 'camunda-tasklist/pile/data'
], function(angular,   uuid,   fix,        _,            $) {

  var mockedModule = angular.module('cam.tasklist.pile.data');

  var _mockedPiles = {
    'pile-1': {
      name: 'Overdue',
      description: '',
      filters: [
        {
          key: 'due',
          operator: 'smaller',
          value: '{now}'
        }
      ],
      color: '#FFB4B4'
    },
    'pile-2': {
      name: 'Due in 3 days',
      description: '',
      filters: [
        {
          key: 'due',
          operator: 'smaller',
          value: '{now} + ({day} * 3)'
        }
      ],
      color: '#FFD2D2'
    },
    'pile-3': {
      name: 'Mines',
      description: '',
      filters: [
        {
          key: 'assignee',
          value: '{self}'
        }
      ],
      color: '#AFB3E2'
    },
    'pile-4': {
      name: 'Group A',
      description: '',
      filters: [
        {
          key: 'identityLink',
          operator: 'has',
          value: 'group-a'
        }
      ],
      color: ''
    },
    'pile-5': {
      name: 'Group B',
      description: '',
      filters: [
        {
          key: 'identityLink',
          operator: 'has',
          value: 'group-a'
        }
      ],
      color: ''
    }
  };




  // sexyness: https://github.com/appendto/jquery-mockjax
  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: '/tasklist/piles',
    response: function() {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/piles'
          }
        },
        _embedded: {
          piles: _.toArray(_mockedPiles)
        }
      };

      this.responseText = JSON.stringify(hal);
    }
  });

  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: /\/tasklist\/piles\/([0-9a-z-]+)$/g,
    data: ['pileId'],
    response: function(settings) {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/piles/'+ settings.pileId
          }
        }
      };

      _.extend(hal, _mockedPiles[settings.pileId]);

      this.responseText = JSON.stringify(hal);
    }
  });

  return mockedModule;
});
