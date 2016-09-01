/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.batch;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.impl.util.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

/**
 * Common methods for batch job handlers, providing serialization, configuration instantiation, etc.
 *
 * @author Askar Akhmerov
 */
public abstract class AbstractBatchJobHandler <T extends AbstractBatchConfiguration> implements BatchJobHandler<T> {

  @Override
  public void deleteJobs(BatchEntity batch) {
    List<JobEntity> jobs = Context.getCommandContext()
        .getJobManager()
        .findJobsByJobDefinitionId(batch.getBatchJobDefinitionId());

    for (JobEntity job : jobs) {
      job.delete();
    }
  }

  @Override
  public BatchJobConfiguration newConfiguration(String canonicalString) {
    return new BatchJobConfiguration(canonicalString);
  }

  @Override
  public void onDelete(BatchJobConfiguration configuration, JobEntity jobEntity) {
    String byteArrayId = configuration.getConfigurationByteArrayId();
    if (byteArrayId != null) {
      Context.getCommandContext().getByteArrayManager()
          .deleteByteArrayById(byteArrayId);
    }
  }

  protected ByteArrayEntity saveConfiguration(ByteArrayManager byteArrayManager, T jobConfiguration) {
    ByteArrayEntity configurationEntity = new ByteArrayEntity();
    configurationEntity.setBytes(writeConfiguration(jobConfiguration));
    byteArrayManager.insert(configurationEntity);
    return configurationEntity;
  }

  @Override
  public byte[] writeConfiguration(T configuration) {
    JSONObject jsonObject = getJsonConverterInstance().toJsonObject(configuration);

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    Writer writer = StringUtil.writerForStream(outStream);

    jsonObject.write(writer);
    IoUtil.flushSilently(writer);

    return outStream.toByteArray();
  }

  @Override
  public T readConfiguration(byte[] serializedConfiguration) {
    Reader jsonReader = StringUtil.readerFromBytes(serializedConfiguration);
    return getJsonConverterInstance().toObject(new JSONObject(new JSONTokener(jsonReader)));
  }

  protected abstract JsonObjectConverter<T> getJsonConverterInstance ();
}
