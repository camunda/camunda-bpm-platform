/* global ngDefine: false */

/**
 * @namespace cam.common.services
 */
ngDefine('camunda.common.services', [
  'module:camunda.common.services.authentication:./Authentication',
  'module:camunda.common.services.debounce:./debounce',
  'module:camunda.common.services.notifications:./Notifications',
  'module:camunda.common.services.resolver:./ResourceResolver',
  'module:camunda.common.services.uri:./uri',
  './engineRequestHeaderProvider',
  './RequestLogger',
  './RequestStatusInterceptor'
], function() {});
