'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'uuid', 'fixturer', 'underscore', 'jquery', 'camunda-tasklist/pile/data'
], function(angular,   uuid,   fix,        _,            $) {

  var mockedModule = angular.module('cam.tasklist.pile.data');

  var _mockedPiles = {};
  _.each([
    {
      id: uuid(),
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
    {
      id: uuid(),
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
    {
      id: uuid(),
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
    {
      id: uuid(),
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
    {
      id: uuid(),
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
  ], function(pile) {
    _mockedPiles[pile.id] = pile;
  });





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
      console.info('pile details', settings.url.match(/\/tasklist\/piles\/([0-9a-z-]+)$/g));
      var hal = {
        _links: {
          self: {
            href: settings.url //'/tasklist/piles/'+ settings.pileId
          }
        }
      };

      _.extend(hal, _mockedPiles[settings.pileId]);

      this.responseText = JSON.stringify(hal);
    }
  });

  return mockedModule;
});
