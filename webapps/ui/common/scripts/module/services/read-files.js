'use strict';

module.exports = ['$q', '$window', function($q, $window) {
  return function(files) {
    return $q.all(
      Array.prototype.map.call(files, readFile)
    );
  };

  function readFile(file)  {
    var deferred = $q.defer();

    var reader = new $window.FileReader();

    reader.onload = function(e) {
      deferred.resolve({
        file: file,
        content: e.target.result
      });
    };

    reader.onerror = function(error) {
      deferred.reject(error);
    };

    reader.readAsText(file);

    return deferred.promise;
  }
}];
