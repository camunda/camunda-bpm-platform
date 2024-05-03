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
import path from "path";
import got from "got";
import FormData from "form-data";
import { startCamunda } from "run-camunda/camunda.js";

const deploy = async filePath => {
  // constants
  const DEPLOYMENT_NAME = "TEST_PROCESS_LOANS";
  const URL = "http://localhost:8080/engine-rest/deployment/create";

  // create form and deploy
  const form = new FormData();
  form.append("deployment-name", DEPLOYMENT_NAME);
  form.append(path.basename(filePath), fs.createReadStream(filePath));
  try {
    await got.post(URL, { body: form });
  } catch (e) {
    throw e.response ? e.response.body.message : e;
  }
};

const startProcess = async definitionKey => {
  try {
    await got.post(
      `http://localhost:8080/engine-rest/process-definition/key/${definitionKey}/start`,
      { json: {} }
    );
  } catch (e) {
    throw e.response ? e.response.body : e;
  }
};

const setup = async () => {
  await startCamunda();
  console.log("deploying process ...");
  await deploy("./test-process.bpmn");
  console.log("process deployed");
  console.log("starting process ...");
  await startProcess("loan_process");
  console.log("process started");
};

setup();
