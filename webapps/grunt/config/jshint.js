module.exports = function() {
  return {
    options: {
      jshintrc: true
    },
    scripts: {
      files: {
        src: [
          'client/scripts/**/*.js'
        ]
      }
    },
    test: {
      files: {
        src: [
          'test/**/*.js'
        ]
      }
    }
  };
};
