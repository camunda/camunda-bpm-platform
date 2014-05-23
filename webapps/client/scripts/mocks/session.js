'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'fixturer', 'underscore', 'jquery', 'camunda-tasklist/session/data'
], function(angular,   fix,        _,            $) {

  var mockedModule = angular.module('cam.tasklist.session.data');

  var _mockedSessions = {};
  var id;

  if ($('base').attr('href') === '/') {
    $.mockjax({
      type: 'POST',
      contentType: 'application/json;charset=utf-8',
      url: '/camunda/api/admin/auth/user/default/login/tasklist',
      data: {
        username: 'jonny1',
        password: 'jonny1'
      },
      response: function() {
        this.status = 201;
        this.contentType = 'application/json';
        this.responseText = JSON.stringify({
          userId: 'jonny1',
          authorizedApps: [
            'cockpit',
            'admin',
            'tasklist'
          ]
        });
      }
    });
  }

  $.mockjax({
    type: 'POST',
    contentType: 'application/json;charset=utf-8',
    url: '/tasklist/sessions',
    data: {
      username: 'jonny1',
      password: 'jonny1'
    },
    response: function() {
      this.status = 201;
      this.contentType = 'application/json';
      this.responseText = JSON.stringify({
        userId: 'jonny1',
        authorizedApps: [
          'cockpit',
          'admin',
          'tasklist'
        ]
      });
    }
  });

  $.mockjax({
    type: 'POST',
    contentType: 'application/json;charset=utf-8',
    url: '/tasklist/sessions',
    response: function() {
      this.status = 401;
      this.responseText = '';
    }
  });

  return mockedModule;
});
