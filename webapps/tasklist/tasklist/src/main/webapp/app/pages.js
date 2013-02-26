"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("tasklist.pages", [
    "tasklist/pages/login",
    "tasklist/pages/logout",
    "tasklist/pages/overview"
  ]);
});