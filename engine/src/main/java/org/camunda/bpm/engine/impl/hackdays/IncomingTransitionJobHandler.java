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
package org.camunda.bpm.engine.impl.hackdays;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class IncomingTransitionJobHandler implements JobHandler<JobHandlerConfiguration> {

  public static final String TYPE = "incoming-transition";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(JobHandlerConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    ActivityInstanceGenerator generator = new ActivityInstanceGenerator(commandContext);

    TransitionInstance transitionInstance = generator.buildActivityInstanceTreeAndReturnTransition(execution);
    transitionInstance.setState(TransitionInstanceState.AFTER_ASYNC);
    EventLoop.run(transitionInstance);
  }

  @Override
  public JobHandlerConfiguration newConfiguration(String canonicalString) {
    return new JobHandlerConfiguration() {

      @Override
      public String toCanonicalString() {
        return null;
      }
    };
  }

  /* (non-Javadoc)
   * @see org.camunda.bpm.engine.impl.jobexecutor.JobHandler#onDelete(org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration, org.camunda.bpm.engine.impl.persistence.entity.JobEntity)
   */
  @Override
  public void onDelete(JobHandlerConfiguration configuration, JobEntity jobEntity) {
    // TODO Auto-generated method stub

  }



}
