module.exports = function() {
  return {
    options: {
      jshintrc: true
    },
    client: {
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
