/* global ngDefine: false */

/**
 * @namespace cam.cockpit.plugin
 */

/**
 * @namespace cam.cockpit.plugin.base
 */
ngDefine('cockpit.plugin.base', [
  'module:cockpit.plugin.base.views:./views/main',
  'module:cockpit.plugin.base.resources:./resources/main',
  'module:cockpit.plugin.base.data:./data/main'
], function(module) {
  return module;
});
