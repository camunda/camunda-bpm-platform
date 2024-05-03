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
import { Client, logger, Variables, File } from "../index.js";

describe("integration", () => {
  let client, expectedScore, expectedUser, expectedTextFile, expectedBinaryFile;

  beforeAll(() => {
    const config = {
      baseUrl: "http://localhost:8080/engine-rest",
      use: logger
    };

    expectedScore = 6;
    expectedUser = { name: "Jean Pierre", balance: "$2000" };
    expectedTextFile = fs.readFileSync("./test-file.txt").toString("utf-8");
    expectedBinaryFile = fs.readFileSync("./test-file.png");
    // create a Client instance with custom configuration
    client = new Client(config);
  });

  afterAll(() => {
    client.stop();
  });

  test("should subscribe client and complete with process variables", () => {
    // susbscribe to the topic: 'creditScoreChecker'
    return new Promise((resolve, reject) => {
      client.subscribe("creditScoreChecker", async function({
        task,
        taskService
      }) {
        try {
          const textAttachment = await new File({
             localPath: "./test-file.txt",
             encoding: "utf-8",
             mimetype: "text/plain",
          }).load();
          const binaryAttachment = await new File({
             localPath: "./test-file.png",
             encoding: "utf-8",
             mimetype: "image/png",
          }).load();
          const processVariables = new Variables()
            .set("score", expectedScore)
            .set("user", expectedUser)
            .set("textAttachment", textAttachment)
            .set("binaryAttachment", binaryAttachment);
          await taskService.complete(task, processVariables);
          resolve();
        } catch (e) {
          reject(e);
        }
      });
    });
  });

  test("should subscribe client, receive process variables and handle failure", () => {
    // susbscribe to the topic: 'creditScoreChecker'
    return new Promise((resolve, reject) => {
      client.subscribe("loanGranter", async function({ task, taskService }) {
        try {
          const { score, user, textAttachment, binaryAttachment } = task.variables.getAll();
          expect(score).toBe(expectedScore);
          expect(user).toEqual(expectedUser);
          expect((await textAttachment.load()).content.toString("utf-8")).toEqual(expectedTextFile);
          expect((await binaryAttachment.load()).content).toEqual(expectedBinaryFile);
          await taskService.handleFailure(task, {
            errorMessage: "something failed"
          });
          resolve();
        } catch (e) {
          reject(e);
        }
      });
    });
  });
});
