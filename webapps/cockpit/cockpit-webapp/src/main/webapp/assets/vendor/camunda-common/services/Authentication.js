"use strict";

ngDefine('camunda.common.services.authentication', function(module, angular) {

  var ServiceProducer = function AuthenticationFactory($http, $cookies, $q, Uri) {

    var AUTH_COOKIE_NAME = "CAM-AUTH";

    var readAuthFromCookie = function(self) {
      
      self.auth = {};

      var cookieValue = $cookies[AUTH_COOKIE_NAME];
      if(!!cookieValue) {
        var parsedCookie = JSON.parse(cookieValue.replace(/^"|"$|\\/g, ''));
        self.auth.username = parsedCookie[Uri.appUri(':engine')];       
      } 
    };
 
    function Authentication() {
      readAuthFromCookie(this);
    }

    Authentication.prototype.username = function() {
      return this.auth.username;
    };

    Authentication.prototype.clear = function() {
      this.auth = {};
    };

    Authentication.prototype.login = function(username, password) {
      var self = this;

      var form = $.param({ 'username': username, 'password': password });

      var promise = $http({
        method: 'POST',
        url: Uri.appUri("admin://auth/user/:engine/login"),
        data: form,
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });

      return promise.then(function(response) {
        if (response.status == 200) {
          self.auth.username = username;      
          return true;
        } else {
          return false;
        }
      }, function(error) {
        return false;
      });
    };

    Authentication.prototype.logout = function() {
      var self = this,
          promise = $http.post(Uri.appUri("admin://auth/user/:engine/logout"));

      return promise.then(function() {
        self.clear();        
        return true;
      });
    };

    return new Authentication();
  };


  ServiceProducer.$inject = [ '$http', '$cookies', '$q', 'Uri' ];

  module.service('Authentication', ServiceProducer);
});