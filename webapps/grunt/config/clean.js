module.exports = function(config) {
  config = config || {};

  return {
    build: {
      src: [
        'doc',
        'dist',
        'target',
        '.tmp',
        '.bower_packages'
      ]
    },
    sdk: {
      src: [
        '.bower_packages'
      ]
    }
  };
};
