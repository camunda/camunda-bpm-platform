"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.services");

  function AuthenticationFactory($http, Uri) {

    function Authentication() {
      this.auth = { user: null };
    };

    Authentication.prototype.current = function() {
      return this.auth;
    };

    Authentication.prototype.login = function(username, password) {
      var self = this,
          promise = $http.get(Uri.appUri("api/auth/login/" + username + "/" + password));

      return promise.then(function(response) {
        var data = response.data;

        if (data && data.success) {
          self.user = data.user;
        }

        return data && data.success;
      });
    };

    Authentication.prototype.logout = function(username, password) {
      var self = this,
          promise = $http.get(Uri.appUri("api/auth/logout"));

      return promise.then(function() {
        self.auth.user = null;
        return true;
      });
    };

    return new Authentication();
  };

  AuthenticationFactory.$inject = ["$http", "Uri"];

  module.factory("Authentication", AuthenticationFactory);

  return module;
});