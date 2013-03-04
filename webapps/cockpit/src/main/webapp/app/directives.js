"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("cockpit.directives", [
    "cockpit/directives/processDiagram",
    "cockpit/directives/adjustHeightOnResize",
    "cockpit/directives/hideElement"
  ]);
});
