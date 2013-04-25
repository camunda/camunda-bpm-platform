"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("cockpit.directives", [
    "cockpit/directives/fillHeight",
    "cockpit/directives/fillWidth",
    "cockpit/directives/hidePanel",
    "cockpit/directives/pageOverflowHidden",
    "cockpit/directives/processDiagram",
    "cockpit/directives/miniatureProcessDiagram",
  ]);
});
