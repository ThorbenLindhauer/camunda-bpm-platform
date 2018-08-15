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
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class UserTaskTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Test
  @Deployment
  public void shouldCompleteUserTask()
  {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    TaskService taskService = engineRule.getTaskService();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    assertThat(processInstance).isNotNull();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  /*
   * Findings:
   *
   * - situation bei async after:
   *   - es gibt einen Job pro ausgehendem Sequenzfluß
   *   - execution steht noch auf der letzten Aktivität
   *   - transition id ist in die Job-Konfiguration kodiert
   */

}
