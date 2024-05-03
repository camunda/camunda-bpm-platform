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

import fs from "fs";
import util from "util";
import path from "path";

import { MISSING_FILE_OPTIONS } from "./__internal/errors.js";

class File {
  /**
   * @throws Error
   * @param options
   * @param options.localPath
   * @param options.remotePath
   * @param options.typedValue
   * @param options.filename
   * @param options.encoding
   * @param options.mimetype
   * @param options.engineService
   */
  constructor(options = {}) {
    this.load = this.load.bind(this);
    this.createTypedValue = this.createTypedValue.bind(this);
    this.__readFile = util.promisify(fs.readFile);

    const { localPath, remotePath, typedValue } = options;

    if (!localPath && !remotePath && !typedValue) {
      throw new Error(MISSING_FILE_OPTIONS);
    }

    Object.assign(this, options);

    if (typedValue) {
      Object.assign(this, typedValue.valueInfo);
      delete this.typedValue;
    }

    this.filename = this.filename || path.basename(remotePath || localPath);
    this.content = "";
  }

  /**
   * Reads file from localPath
   * @throws Error
   */
  async load() {
    // get content either locally or from remotePath
    this.content = this.remotePath
      ? await this.engineService.get(this.remotePath)
      : await this.__readFile(this.localPath);

    return this;
  }

  createTypedValue() {
    const valueInfo = { filename: this.filename };
    if (this.encoding) {
      valueInfo.encoding = this.encoding;
    }
    if (this.mimetype) {
      valueInfo.mimetype = this.mimetype;
    }
    return {
      type: "file",
      value: this.content.toString("base64"),
      valueInfo,
    };
  }
}

export default File;
