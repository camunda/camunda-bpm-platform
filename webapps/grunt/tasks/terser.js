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

const terser = require('terser');
const abbreviateNumber = require('../../camunda-commons-ui/lib/filter/abbreviateNumber')();
const path = require('path');

function getBinarySize(string) {
  return Buffer.byteLength(string, 'utf8');
}

module.exports = function(grunt) {
  grunt.registerMultiTask('terser', async function() {
    const done = this.async();
    let fileCounter = 0;
    let originalSize = 0;
    let finalSize = 0;
    // Minify the files in parallel
    await Promise.all(
      this.files.map(async file => {
        const original = file.src.reduce((src, path) => {
          src[path] = grunt.file.read(path);
          originalSize += getBinarySize(src[path]);
          return src;
        }, {});

        const licenses = new Set();
        const minified = await terser.minify(original, {
          format: {
            comments: (astNode, comment) => {
              const hasLicense =
                /^\**!/i.test(comment.value) || // include license headers with a leading ! (e.g., moment.js)
                (/@license|@preserve|@lic|@cc_on/i.test(comment.value) &&
                  'comment2' === comment.type);

              if (hasLicense) {
                licenses.add(comment.value);
              }
              return false;
            }
          }
        });

        if (minified.error) {
          grunt.log.error(minified.error);
          return false;
        }
        if (minified.warnings) {
          grunt.log.warn(minified.warnings.join('\n'));
        }

        if (licenses.size > 0) {
          const filename = path.parse(file.dest).base;
          grunt.file.write(
            file.dest,
            `/*! For license information, please see ${filename}.LICENSE.txt */\n${minified.code}`
          );

          grunt.file.write(
            `${file.dest}.LICENSE.txt`,
            Array.from(licenses)
              .map(license => `/*${license}*/\n\n`)
              .join('')
          );
        } else {
          grunt.file.write(file.dest, minified.code);
        }

        finalSize += getBinarySize(minified.code);
        fileCounter++;
      })
    );

    grunt.log.ok(
      `${fileCounter} files created. ` +
        `${abbreviateNumber(originalSize, 2)}B --> ` +
        `${abbreviateNumber(finalSize, 2)}B`
    );

    done();
  });
};
