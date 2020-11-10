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

var checker = require('license-checker');

const PRODUCTION_LICENSES = [
  '0BSD',
  'Apache 2.0',
  'Apache-2.0',
  'Apache-2.0 WITH LLVM-exception',
  'BSD',
  'BSD-2-Clause',
  'BSD-3-Clause',
  'CC0-1.0',
  'ISC',
  'MIT',
  'WTFPL'
];

const DEV_LICENSES = [
  'CC-BY-3.0',
  'CC-BY-4.0',
  'ODC-By-1.0',
  'Unlicense',
  'Zlib'
];

const ALLOWED_PACKAGES = [
  'axe-core@3.5.5', // uses MPL-2.0, permitted as of https://jira.camunda.com/browse/OB-8
  'desired-capabilities@0.1.0', // uses the CC0, but has wrong license field
  'jsonify@0.0.0', // uses the unlicense, but has wrong license field
  'map-stream@0.1.0', // uses the MIT, but has wrong license field
  'stackframe@0.3.1' // uses the MIT, but has wrong license field
];

const parseResults = (ALLOWED_LICENSES, resolve, reject) =>
  function(err, packages) {
    if (err) {
      throw err;
    } else {
      const entries = Object.entries(packages);
      let licenseWarning = '';

      for (const [id, info] of entries) {
        if (ALLOWED_PACKAGES.includes(id)) {
          continue;
        }

        let licenses = info.licenses;
        let hasMultipleLicenses = false;

        if (typeof licenses === 'string') {
          licenses = licenses.replace(/\(|\)|\*/g, '');
          if (licenses.includes('AND')) {
            licenses = licenses.split(' AND ');
            hasMultipleLicenses = true;
          } else {
            licenses = licenses.split(' OR ');
          }
        }

        licenses = typeof licenses === 'object' ? licenses : [licenses];

        let approved = hasMultipleLicenses
          ? licenses.every(license => ALLOWED_LICENSES.includes(license))
          : licenses.some(license => ALLOWED_LICENSES.includes(license));

        if (!approved) {
          licenseWarning += `${id} uses ${licenses.join(' OR ')}\n`;
        }
      }

      if (licenseWarning) {
        reject(licenseWarning);
      } else {
        resolve();
      }
    }
  };

module.exports = function(grunt) {
  grunt.registerTask('license-check', function() {
    const done = this.async();

    const licenseChecks = [
      new Promise((resolve, reject) => {
        checker.init(
          {
            start: '.',
            production: true,
            excludePrivatePackages: true
          },
          parseResults(PRODUCTION_LICENSES, resolve, reject)
        );
      }),
      new Promise((resolve, reject) => {
        checker.init(
          {
            start: '.',
            development: true,
            excludePrivatePackages: true
          },
          parseResults(
            [...PRODUCTION_LICENSES, ...DEV_LICENSES],
            resolve,
            reject
          )
        );
      })
    ];

    Promise.all(licenseChecks)
      .then(() => {
        done();
      })
      .catch(err => {
        console.error(err);
        done(false);
      });
  });
};
