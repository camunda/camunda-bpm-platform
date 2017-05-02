module.exports = ['$window', function($window) {
  return function() {
    const {FileReader} = $window;

    return typeof FileReader === 'function' && typeof FileReader.prototype.readAsText === 'function';
  };
}];
