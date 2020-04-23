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

const fs = require('fs');
var checker = require('license-checker');

const header = `THIRD-PARTY SOFTWARE NOTICES AND INFORMATION
Do Not Translate or Localize

This project incorporates components from the projects listed below. The original copyright notices and the licenses under which Camunda received such components are set forth below.

`
module.exports = function(grunt) {

  var path = grunt.option('license-book-path') || '.';

  grunt.registerTask('license-book', function() {
    var done = this.async();

    checker.init({
        start: '.',
        production: true,
        excludePrivatePackages: true
    }, function(err, packages) {
        if (err) {
          throw err;
        } else {
          var licenseTexts = '';
          var summary = '';
          var i = 0;
          const entries = Object.entries(packages)

          for (const [package, info] of entries) {
            var dep_license;
            if(!info.licenseFile) {
              console.warn('Missing lincense File for ' + package);
              dep_license = `${info.licenses}`;
            } else {
              summary += `${rightPad(i++ + '.' , 6)}${package} (${info.repository || info.url}) \n`
              dep_license = fs.readFileSync(info.licenseFile);}

              licenseTexts +=

`
${package} NOTICES AND INFORMATION BEGIN HERE
==========================================
` +
dep_license
  +
`
==========================================
END OF ${package} NOTICES AND INFORMATION


`
          }

          var licenseBook = header + summary + '\n\n' + licenseTexts;
          fs.writeFileSync(path + '/THIRD-PARTY-NOTICE.txt', licenseBook);
          done();
        }
    });

  });
};


function rightPad (string, length, char) {
  var i = -1;
  length = length - string.length;
  if (!char && char !== 0) {
    char = ' ';
  }
  while (++i < length) {
    string += char;
  }

  return string;
}