'use strict';

var callActivityOverlay = require('../../../../../../client/scripts/components/callActivityOverlay')('runtime');

module.exports = ['ViewsProvider',  function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.plugin', {
    id: 'activity-instance-call-activity-overlay',
    overlay: callActivityOverlay
  });
}];
