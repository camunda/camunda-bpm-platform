'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'uuid', 'fixturer', 'underscore', 'jquery', 'camunda-tasklist/session/data'
], function(angular,   uuid,   fix,        _,            $) {

  var mockedModule = angular.module('cam.tasklist.session.data');

  var _mockedSessions = {};
  var id;

  // sexyness: https://github.com/appendto/jquery-mockjax
  $.mockjax({
    method: 'GET',
    contentType: 'application/hal+json',

    url: '/tasklist/sessions',
    response: function() {
      var hal = {
        _links: {
          self: {
            href: '/tasklist/sessions'
          }
        },
        _embedded: {
          sessions: _.toArray(_mockedSessions)
        }
      };

      this.responseText = JSON.stringify(hal);
    }
  });

  $.mockjax({
    method: 'POST',
    contentType: 'application/hal+json',

    url: '/tasklist/sessions',
    response: function() {

      console.info(this);

      this.status = 201;

      // var hal = {
      //   _links: {
      //     self: {
      //       href: '/tasklist/sessions'
      //     }
      //   },
      //   _embedded: {
      //     sessions: _.toArray(_mockedSessions)
      //   }
      // };

      // this.responseText = JSON.stringify(hal);
    }
  });

  return mockedModule;
});
