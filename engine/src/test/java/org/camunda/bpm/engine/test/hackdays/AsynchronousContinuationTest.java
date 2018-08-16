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
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class AsynchronousContinuationTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();
  private RuntimeService runtimeService;
  private ManagementService managementService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  @Test
  @Deployment
  public void shouldCompleteAsyncBefore()
  {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();

    // when
    managementService.executeJob(job.getId());

    // then
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }


  @Test
  @Deployment
  public void shouldRunAsyncBeforeInSuccession()
  {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    job = managementService.createJobQuery().singleResult();

    // when
    managementService.executeJob(job.getId());

    // then
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }


  @Test
  @Deployment
  public void shouldCompleteAsyncAfter()
  {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();

    // when
    managementService.executeJob(job.getId());

    // then
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }
}
