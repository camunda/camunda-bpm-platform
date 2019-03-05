let { promisify } = require('util');
let exec = promisify(require('child_process').exec);
let path = require('path');
let superagentE2ePath = '../camunda-bpm-sdk-js/vendor/superagent';

module.exports = function(grunt, isCeEdition) {
  grunt.registerTask('compileLibs', function() {
    let libs = [];

    let libDir = null;
    if (!isCeEdition) {
      libDir = '..';

      libs.push('../camunda-commons-ui/node_modules/camunda-bpm-sdk-js/vendor/fast-xml-parser');
      libs.push('../camunda-commons-ui/node_modules/camunda-bpm-sdk-js/vendor/superagent');
      libs.push('../camunda-bpm-sdk-js/vendor/fast-xml-parser');
      libs.push(superagentE2ePath);
    } else {
      libDir = 'node_modules';
    }

    libs = libs.concat([
      'node_modules/camunda-bpm-sdk-js/vendor/fast-xml-parser',
      'node_modules/camunda-bpm-sdk-js/vendor/superagent',
      libDir + '/camunda-commons-ui/bpmn-js',
      libDir + '/camunda-commons-ui/dmn-js',
      libDir + '/camunda-commons-ui/cmmn-js'
    ]);

    let done = this.async();

    let cmd = null;
    /*if (process.platform === 'win32') {
      cmd = cmd.replace(/\//g, '\\');
    }*/

    let builds = libs.map(lib => {
      if (superagentE2ePath === lib) {
        cmd = 'npm run buildE2e';
      } else {
        cmd = 'npm run build';
      }

      let libPath = path.join(__dirname, `../../${lib}/`);
    return exec(cmd, { maxBuffer: 1024 * 500, cwd: libPath });
  });

    Promise.all(builds)
      .then(() => done())
  .catch(console.error);
  });
};
