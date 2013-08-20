ngDefine('camunda.common.services.authentication', function(module) {

  var ServiceProducer = [ '$rootScope', '$http', '$cookies', 'Uri', '$cacheFactory',
  function AuthenticationFactory($rootScope, $http, $cookies, Uri, $cacheFactory) {

    function Authentication() {
      $rootScope.authentication = this;

      var self = this;

      this.username = function() {
        return (self.user || {}).name;
      }
    }

    Authentication.prototype.isCockpitAuthorized = function() {
      return !this.user || (this.user.authorizedApps && this.user.authorizedApps.indexOf('cockpit') !== -1);
    };

    Authentication.prototype.isTasklistAuthorized = function() {
      return !this.user || (this.user.authorizedApps && this.user.authorizedApps.indexOf('tasklist') !== -1);
    };

    Authentication.prototype.clear = function() {
      this.user = null;
    };

    Authentication.prototype.update = function(data) {
      var user = this.user;

      if ((!user && data) || (user && data && data.name != user.name)) {
        this.user = data;
      }
    };

    Authentication.prototype.login = function(username, password) {
      var self = this;

      var form = $.param({ 'username': username, 'password': password });

      var promise = $http({
        method: 'POST',
        url: Uri.appUri("admin://auth/user/:engine/login/:appName"),
        data: form,
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });

      return promise.then(function(response) {
        if (response.status == 200) {
          self.update({
            name: response.data.userId,
            authorizedApps: response.data.authorizedApps
          });
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
          promise = $http.post(Uri.appUri('admin://auth/user/:engine/logout'));

      return promise.then(function() {
        // clear authentication
        self.clear();

        // clear cache
        $cacheFactory.get("$http").removeAll();

        return true;
      });
    };

    return new Authentication();
  }];

  module.service('Authentication', ServiceProducer);
});