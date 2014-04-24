/* global define: false, angular: false */
define([
  './email',
  './engineSelect',
  './help',
  './inPlaceTextField',
  // './paginator',
  './password',
  './requestAware',
  './ifLoggedIn',
  './bootstrap',
  './notificationsPanel',
  './showIfAuthorized'
], function() {
  'use strict';
  angular.module('camunda.common.directives', [
    'camunda.common.directives.ifLoggedIn',
    'camunda.common.directives.bootstrap',
    'camunda.common.directives.notificationsPanel',
    'camunda.common.directives.showIfAuthorized'
  ]);
});
