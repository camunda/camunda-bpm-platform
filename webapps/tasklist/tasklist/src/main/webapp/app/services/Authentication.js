"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.services");

  function AuthenticationFactory($http, $cookies, Uri) {

    function Authentication() {
      this.auth = { current: $cookies.user };
    };

    Authentication.prototype.current = function() {
      return this.auth.current;
    };

    Authentication.prototype.set = function(current) {
      this.auth.current = $cookies.user = current;
    };

    Authentication.prototype.login = function(username, password) {
      var self = this,
          promise = $http.get(Uri.appUri("api/auth/login/" + username + "/" + password));

      return promise.then(function(response) {
        var data = response.data;

        if (data && data.success) {
          self.set(data.user);
        }

        return data && data.success;
      });
    };

    Authentication.prototype.logout = function() {
      var self = this,
          promise = $http.get(Uri.appUri("api/auth/logout"));

      return promise.then(function() {
        self.set(null);
        return true;
      });
    };

    return new Authentication();
  };

  AuthenticationFactory.$inject = ["$http", "$cookies", "Uri"];

  module.factory("Authentication", AuthenticationFactory);

  return module;
});