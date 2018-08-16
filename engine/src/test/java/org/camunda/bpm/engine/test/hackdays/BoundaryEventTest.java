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
package org.camunda.bpm.engine.test.hackdays;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class BoundaryEventTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Test
  @Deployment
  public void shouldTriggerMessage()
  {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.correlateMessage("message");

    // then
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("afterBoundary");

    // when (2)
    engineRule.getTaskService().complete(task.getId());

    // then (2)
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

  }
}
