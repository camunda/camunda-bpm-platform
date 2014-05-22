'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'uuid', 'fixturer', 'underscore', 'jquery', 'camunda-tasklist/user/data'
], function(angular,   uuid,   fix,        _,            $) {

  var mockedModule = angular.module('cam.tasklist.user.data');

  var _mockedUsers = {};
  var id;

  for (var i = 0; i < 20; i++) {
    id = uuid();
    _mockedUsers[id] = fix({
      id: id,
      name: '<%= personName() %>'
    });
  }

  // sexyness: https://github.com/appendto/jquery-mockjax
  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: '/tasklist/users',
    response: function() {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/users'
          }
        },
        _embedded: {
          users: _.toArray(_mockedUsers)
        }
      };

      this.responseText = JSON.stringify(hal);
    }
  });

  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: /\/tasklist\/users\/([0-9a-z-]+)$/g,
    data: ['userId'],
    response: function(settings) {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/users/'+ settings.userId
          }
        }
      };

      _.extend(hal, _mockedUsers[settings.userId]);

      this.responseText = JSON.stringify(hal);
    }
  });

  return mockedModule;
});
