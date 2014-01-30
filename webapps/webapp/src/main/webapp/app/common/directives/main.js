/* global ngDefine: false */

/**
 * @namespace cam.common.directives
 */
ngDefine('camunda.common.directives', [
  'module:camunda.common.directives.notificationsPanel:./notificationsPanel',
  './email',
  './help',
  './requestAware',
  './engineSelect',
  './paginator',
  'module:camunda.common.directives.ifLoggedIn:./ifLoggedIn',
  'module:camunda.common.directives.showIfAuthorized:./showIfAuthorized',
  './password',
  './inPlaceTextField',
  'module:camunda.common.directives.modal:./modal'
], function() {});
