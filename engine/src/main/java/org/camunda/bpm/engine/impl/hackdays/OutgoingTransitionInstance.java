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
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class OutgoingTransitionInstance extends TransitionInstance {


  private final TransitionImpl transition;

  public OutgoingTransitionInstance(ScopeActivityInstance parent, ActivityImpl activity, TransitionImpl transition)
  {
    super(parent, activity, parent.getExecution());
    this.transition = transition;
    this.execution.setActivity(activity);
  }


  public OutgoingTransitionInstance(ScopeActivityInstance parent, ExecutionEntity execution, ActivityImpl activity, TransitionImpl transition)
  {
    super(parent, activity, execution);
    this.transition = transition;
    this.execution.setActivity(activity);
  }

  public ActivityImpl getActivity() {
    return activity;
  }

  public void remove()
  {
    parent.removeChild(this);
    this.execution.setActivity(null);
  }

  public ScopeActivityInstance getParent() {
    return parent;
  }

  public TransitionImpl getTransition() {
    return transition;
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("outgoing transition instance at activity ");
    sb.append(activity.getId());
    return sb.toString();
  }


  @Override
  public boolean isAsync() {
    return activity.isAsyncAfter();
  }


  @Override
  public void createJob() {
    AsyncAfterJobDeclaration declaration = new AsyncAfterJobDeclaration();
    MessageEntity job = declaration.createJobInstance(this);
    Context.getCommandContext().getJobManager().send(job);
    execution.addJob(job);
  }

  private class AsyncAfterJobDeclaration extends JobDeclaration<OutgoingTransitionInstance, MessageEntity>
  {

    public AsyncAfterJobDeclaration() {
      super(OutgoingTransitionJobHandler.TYPE);
    }

    @Override
    protected ExecutionEntity resolveExecution(OutgoingTransitionInstance context) {
      return context.execution;
    }

    @Override
    protected MessageEntity newJobInstance(OutgoingTransitionInstance context) {
      MessageEntity message = new MessageEntity();
      message.setExecution(context.execution);

      return message;
    }

    @Override
    protected JobHandlerConfiguration resolveJobHandlerConfiguration(OutgoingTransitionInstance context) {
      return new OutgoingTransitionJobHandler.OutgoingTransitionConfiguration(
          context.transition != null ? context.transition.getId() : null);
    }

  }
}
