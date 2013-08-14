ngDefine('cockpit.pages', [
  './dashboard', 
  'module:cockpit.pages.processInstance:./processInstance',
  'module:cockpit.pages.processDefinition:./processDefinition',
  './cancelProcessInstance',
  './jobRetries',
  './addVariable'
], function(module) {

});