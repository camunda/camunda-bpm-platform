"use strict";

define([ "angularModule" ], function(angularModule) {

  return angularModule("common.services", [
    "common/services/debouncer",
    "common/services/errors",
    "common/services/httpStatusInterceptor",
    "common/services/httpUtils",
    "common/services/requestStatus",
    "common/services/uri"
  ]);
});