define(['angular',
  'angular-route',
  'camunda-commons-ui',
  './authorizations',
  './authorizationCreate',
  './authorizationDeleteConfirm',
  './users',
  './userCreate',
  './userEdit',
  './groups',
  './groupCreate',
  './groupEdit',
  './groupMembershipsCreate',
  './setup',
  './system',
  './systemSettingsGeneral',
  './execution-metrics'
], function(angular,
  angularRoute,
  camundaCommonsUi,
  authorizations,
  authorizationCreate,
  authorizationDeleteConfirm,
  users,
  userCreate,
  userEdit,
  groups,
  groupCreate,
  groupEdit,
  groupMembershipsCreate,
  setup,
  system,
  systemSettingsGeneral,
  executionMetrics) {
  'use strict';

  var ngModule = angular.module('admin.pages', ['ngRoute', 'cam.commons']);

  ngModule.config(authorizations);
  ngModule.controller('AuthorizationCreateController', authorizationCreate);
  ngModule.controller('ConfirmDeleteAuthorizationController', authorizationDeleteConfirm);
  ngModule.config(users);
  ngModule.config(userCreate);
  ngModule.config(userEdit);
  ngModule.config(groups);
  ngModule.config(groupCreate);
  ngModule.config(groupEdit);
  ngModule.controller('GroupMembershipDialogController', groupMembershipsCreate);
  ngModule.config(setup);
  ngModule.config(system);
  ngModule.config(systemSettingsGeneral);
  ngModule.config(executionMetrics);

  return ngModule;
});
