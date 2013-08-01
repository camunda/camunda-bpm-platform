ngDefine('camunda.common.services.authentication', function(module) {

  var ServiceProducer = [ '$rootScope', '$http', '$cookies', 'Uri', '$cacheFactory',
  function AuthenticationFactory($rootScope, $http, $cookies, Uri, $cacheFactory) {

    var AUTH_COOKIE_NAME = "CAM-AUTH";

    function parseCookie(name) {
      var str = $cookies[name],
          cookie;

      if (str) {
        try {
          // replace leading and trailing `"` 
          // from cookie and parse it as JSON
          cookie = JSON.parse(str.replace(/^"|"$|\\/g, ''));
        } catch (e) {
          console.log('[Authentication] Failed to parse camunda cookie');
        }
      }

      return cookie;
    }

    function readFromCookie(engine) {
      
      var cookie = parseCookie(AUTH_COOKIE_NAME),
          name;

      if (cookie) {
        name = cookie[engine];

        if (name) {
          return { 
            name: cookie[engine].userId,
            isCockpitAuthorized: cookie[engine].cockpit,
            isTasklistAuthorized: cookie[engine].tasklist
           };
        }
      } else {
        return null;
      }
    }
 
    function Authentication() {
      var engine = Uri.appUri(':engine');

      this.user = readFromCookie(engine);

      $rootScope.authentication = this;
    }

    Authentication.prototype.username = function() {
      return (this.user || {}).name;
    };

    Authentication.prototype.isCockpitAuthorized = function() {
      return !!this.user ? this.user.isCockpitAuthorized : true;
    };

    Authentication.prototype.isTasklistAuthorized = function() {
      return !!this.user ? this.user.isTasklistAuthorized : true;
    };

    Authentication.prototype.clear = function() {
      this.user = null;
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
          self.user = { 
            name: response.data.userId,
            isCockpitAuthorized: response.data.cockpitAuthorized,
            isTasklistAuthorized: response.data.tasklistAuthorized
          };      
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
        // clear http cache
        $cacheFactory.get("$http").removeAll();
        return true;
      });
    };

    return new Authentication();
  }];

  module.service('Authentication', ServiceProducer);
});