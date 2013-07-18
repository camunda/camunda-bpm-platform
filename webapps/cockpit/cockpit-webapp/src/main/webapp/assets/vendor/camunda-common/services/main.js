ngDefine('camunda.common.services', [
  'module:camunda.common.services.authentication:./Authentication',
  './debouncer',
  'module:camunda.common.services.notifications:./Notifications',
  'module:camunda.common.services.resolver:./ResourceResolver',
  'module:camunda.common.services.uri:./uri',
  './requestStatus',
  './httpStatusInterceptor'
], function(module) {

});