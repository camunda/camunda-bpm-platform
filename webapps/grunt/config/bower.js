module.exports = function() {
  return {
    options: {
      cleanTargetDir: true,
      verbose: true,
      bowerOptions: {
        forceLatest: false
      }
    },
    prod: {}
  };
};
