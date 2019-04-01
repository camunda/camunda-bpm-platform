/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

let { execSync } = require('child_process');
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

    let builds = libs.map(lib => {
      if (superagentE2ePath === lib) {
        cmd = 'npm run buildE2e';
      } else {
        cmd = 'npm run build';
      }

      if (process.platform === 'win32') {
        cmd = cmd.replace(/\//g, '\\');
      }

      let libPath = path.join(__dirname, `../../${lib}/`);
      try {
        console.log(`\t${libPath}: ${cmd}`);
        execSync(cmd, { maxBuffer: 1024 * 500, cwd: libPath });
      }
      catch (error) {
        console.error(error);
      }
    });

    Promise.all(builds)
      .then(() => done())
      .catch(console.error);
  });
};
