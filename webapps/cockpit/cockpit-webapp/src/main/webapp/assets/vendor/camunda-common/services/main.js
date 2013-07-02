ngDefine('camunda.common.services', [
  './debouncer',
  'module:camunda.common.services.notifications:./Notifications',
  'module:camunda.common.services.resolver:./ResourceResolver',
  'module:camunda.common.services.uri:./uri',
  './httpStatusInterceptor',
  './httpUtils',
  './requestStatus'
], function(module) {

});