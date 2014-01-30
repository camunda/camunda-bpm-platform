/* global ngDefine: false */

/**
 * @namespace cam.cockpit.filters
 */
ngDefine('cockpit.filters', [
  'module:cockpit.filters.shorten:./shorten',
  'module:cockpit.filters.abbreviate.number:./abbreviateNumber'
], function() {});
