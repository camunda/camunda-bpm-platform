module.exports = ['$window', function($window) {
  return function() {
    var FileReader = $window.FileReader;

    return typeof FileReader === 'function' && typeof FileReader.prototype.readAsText === 'function';
  };
}];
