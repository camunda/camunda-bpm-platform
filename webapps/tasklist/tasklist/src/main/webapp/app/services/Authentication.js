"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.services");

  module.factory("Authentication", function ($http, Uri) {
      function Authentication() {
      };

      Authentication.prototype.login = function (username, password) {
        return $http.get(Uri.appUri("api/auth/login/"+username+"/"+password));
      }

      return new Authentication();
    }
  );

  return module;
});