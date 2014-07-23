ngDefine('admin.pages', [
  'module:ngRoute:angular-route',
  'module:cam.commons:camunda-commons-ui',
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
  './systemSettingsGeneral'
], function(module) {});
