'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['$window', '$q', '$rootScope', function($window, $q, $rootScope) {
  return function(url, files, fields) {
    var deferred = $q.defer();

    if (!angular.isArray(files)) {
      files = [files];
    }

    fields = fields || {};

    var segments = files.map(function(entry, index) {
      return 'Content-Disposition: form-data; name="data' + index + '"; filename="' +
              entry.file.name +
              '"\r\nContent-Type: text/xml\r\n\r\n' + entry.content + '\r\n';
    });

    segments = segments.concat(
      Object
        .keys(fields)
        .map(function(name) {
          var value = fields[name];

          return 'Content-Disposition: form-data; name="' + name + '"\r\n\r\n' + value + '\r\n';
        })
    );

    var req = new $window.XMLHttpRequest();

    req.onload = function(evt) {
      if(evt.target.readyState === 4) {
        if(evt.target.status === 200) {
          deferred.resolve(JSON.parse(evt.target.responseText));
        } else if(evt.target.status === 401) {
        // broadcast that the authentication changed
          $rootScope.$broadcast('authentication.changed', null);
        // set authentication to null
          $rootScope.authentication = null;
        // broadcast event that a login is required
        // proceeds a redirect to /login
          $rootScope.$broadcast('authentication.login.required');

          deferred.reject(evt);
        } else {
          deferred.reject(evt);
        }
      }
    };

    req.open('post', url, true);

    var sBoundary = '---------------------------' + Date.now().toString(16);
    req.setRequestHeader('Content-Type', 'multipart\/form-data; boundary=' + sBoundary);

    var sData = '--' + sBoundary + '\r\n' + segments.join('--' + sBoundary + '\r\n') + '--' + sBoundary + '--\r\n';

    req.send(sData);

    return deferred.promise;
  };
}];
