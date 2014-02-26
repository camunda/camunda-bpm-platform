/* global ngDefine: false */

/**
 * @namespace cam.common.directives
 */
ngDefine('camunda.common.directives', [
  './email',
  './engineSelect',
  './help',
  './inPlaceTextField',
  './paginator',
  './password',
  './requestAware',
  'module:camunda.common.directives.ifLoggedIn:./ifLoggedIn',
  'module:camunda.common.directives.modal:./modal',
  'module:camunda.common.directives.notificationsPanel:./notificationsPanel',
  'module:camunda.common.directives.showIfAuthorized:./showIfAuthorized'
], function() {});
