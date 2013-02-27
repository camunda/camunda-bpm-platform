"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("tasklist.pages", [
    "tasklist/common/sitebar",
    "tasklist/pages/login",
    "tasklist/pages/logout",
    "tasklist/pages/overview"
  ]);
});