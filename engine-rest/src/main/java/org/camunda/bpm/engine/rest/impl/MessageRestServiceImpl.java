/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl;

import java.text.ParseException;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.MessageRestService;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.util.DtoUtil;

public class MessageRestServiceImpl extends AbstractRestProcessEngineAware implements MessageRestService {

  public MessageRestServiceImpl() {
    super();
  }
  
  public MessageRestServiceImpl(String engineName) {
    super(engineName);
  }
  
  @Override
  public void deliverMessage(CorrelationMessageDto messageDto) {

    if (messageDto.getMessageName() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No message name supplied");
    }
    
    RuntimeService runtimeService = processEngine.getRuntimeService();
    
    try {
      Map<String, Object> correlationKeys = DtoUtil.toMap(messageDto.getCorrelationKeys());
      Map<String, Object> processVariables = DtoUtil.toMap(messageDto.getProcessVariables());
      
      runtimeService.correlateMessage(messageDto.getMessageName(), messageDto.getBusinessKey(), 
          correlationKeys, processVariables);
      
    } catch (MismatchingMessageCorrelationException e) {
      throw new RestException(Status.BAD_REQUEST, e);
      
    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot deliver a message due to number format exception: %s", e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);
      
    } catch (ParseException e) {
      String errorMessage = String.format("Cannot deliver a message due to parse exception: %s", e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);      
    
    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot deliver a message: %s", e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);  
    }

  }

}
