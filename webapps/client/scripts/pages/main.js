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
  './systemSettingsFlowNodeCount'
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
  systemSettingsFlowNodeCount) {
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
  ngModule.config(systemSettingsFlowNodeCount);

  return ngModule;
});
