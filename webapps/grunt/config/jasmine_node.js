module.exports = function() {
  return {
    options: {
      forceExit: true,
      match: '.',
      matchall: false,
      extensions: 'js',
      specNameMatcher: 'Spec',
      captureExceptions: true,
      verbose: true,
      junitreport: {
        report: false,
        savePath : "./test/reports/unit/",
        useDotNotation: true,
        consolidate: true
      }
    },
    unit: ['test/unit/**']
  };
};
