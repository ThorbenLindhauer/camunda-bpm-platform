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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class IncomingTransitionInstance extends TransitionInstance {

  // TODO: it may make sense to separate the use cases of deserialization from database state and creation during process execution
  public IncomingTransitionInstance(ScopeActivityInstance parent, ExecutionEntity execution, ActivityImpl activity)
  {
    super(parent, activity, execution);
    this.execution.setActivity(activity);
  }


  @Override
  public boolean isAsync() {
    return activity.isAsyncBefore();
  }

  @Override
  public void createJob() {
    AsyncBeforeJobDeclaration declaration = new AsyncBeforeJobDeclaration();
    MessageEntity job = declaration.createJobInstance(getExecution());
    Context.getCommandContext().getJobManager().send(job);
    getExecution().addJob(job);
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("incoming transition instance at activity ");
    sb.append(activity.getId());
    return sb.toString();
  }

  public ActivityInstance toActivityInstance()
  {
    final ActivityInstance instance;
    destroy();
    if (activity.isScope())
    {
      instance = new ScopeActivityInstance(parent, getExecution(), activity);
    }
    else
    {
      instance = new NonScopeActivityInstance(parent, getExecution(), activity);
    }

    parent.addChild(instance);
    return instance;
  }

  private class AsyncBeforeJobDeclaration extends JobDeclaration<ExecutionEntity, MessageEntity>
  {

    public AsyncBeforeJobDeclaration() {
      super("incoming-transition");
    }

    @Override
    protected ExecutionEntity resolveExecution(ExecutionEntity context) {
      return context;
    }

    @Override
    protected MessageEntity newJobInstance(ExecutionEntity context) {
      MessageEntity message = new MessageEntity();
      message.setExecution(context);

      return message;
    }

    @Override
    protected JobHandlerConfiguration resolveJobHandlerConfiguration(ExecutionEntity context) {
      return new JobHandlerConfiguration() {

        @Override
        public String toCanonicalString() {
          return null;
        }
      };
    }

  }
}
