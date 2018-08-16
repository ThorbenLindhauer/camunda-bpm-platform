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
import static org.assertj.core.api.Assertions.fail;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

/**
 * @author Nico Rehwaldt
 *
 */
public class SubProcessTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  private RuntimeService runtimeService;
  private TaskService taskService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }


  @Test
  @Deployment
  public void shouldComplete()
  {
    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // then
    assertThat(processInstance.isEnded()).isTrue();

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }


  @Test
  @Deployment
  public void shouldCompleteStepByStep()
  {
    // given
    runtimeService.startProcessInstanceByKey("process");

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);

    // when
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // when (2)
    Task afterBoundaryTask = taskService.createTaskQuery().singleResult();
    assertThat(afterBoundaryTask.getTaskDefinitionKey()).isEqualTo("taskAfterSubProcess");

    taskService.complete(afterBoundaryTask.getId());

    // then (2)
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }


  @Test
  @Deployment
  public void shouldInterruptWithBoundaryEvent()
  {
    fail("model is noch kaputt");

    // given
    runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.correlateMessage("message");
    taskService.complete("taskAfterMessage");

    // then
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

}
