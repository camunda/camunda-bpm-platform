'use strict';

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  userResource = require('./userResource'),
  groupResource = require('./groupResource'),
  groupMembershipResource = require('./groupMembershipResource'),
  initialUserResource = require('./initialUserResource'),
  metricsResource = require('./metricsResource');

  var ngModule = angular.module('admin.resources', []);

  ngModule.factory('UserResource', userResource);
  ngModule.factory('GroupResource', groupResource);
  ngModule.factory('GroupMembershipResource', groupMembershipResource);
  ngModule.factory('InitialUserResource', initialUserResource);
  ngModule.factory('MetricsResource', metricsResource);

  module.exports = ngModule;
