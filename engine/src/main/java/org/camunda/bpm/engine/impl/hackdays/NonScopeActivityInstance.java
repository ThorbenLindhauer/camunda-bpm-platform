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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class NonScopeActivityInstance extends ActivityInstance {

  private ExecutionEntity execution;

  public NonScopeActivityInstance(ScopeActivityInstance parent, ActivityImpl activity, ExecutionEntity attachableExecution) {
    super(parent, activity);
    this.execution = attachableExecution;
    this.execution.setActivity(activity);
    this.execution.enterActivityInstance();
  }

  protected ExecutionEntity destroy() {
    ExecutionEntity execution = getExecution();

    execution.leaveActivityInstance();
    execution.setActivity(activity.getParentFlowScopeActivity());
    parent.removeChild(this);

    return execution;
  }

  @Override
  public ActivityInstance newActivityInstance(ActivityImpl activity) {
    throw new UnsupportedOperationException("a non-scope activity instance cannot have children");
  }

  @Override
  public ExecutionEntity getExecution() {
    ExecutionEntity replacingExecution = execution.resolveReplacedBy();

    return replacingExecution != null ? replacingExecution : execution;
  }

  @Override
  public OutgoingTransitionInstance toOutgoingInstance(TransitionImpl transition) {
    ExecutionEntity attachableExecution = getExecution();
    destroy();
    OutgoingTransitionInstance transitionInstance = new OutgoingTransitionInstance(parent, attachableExecution, activity, transition);
    parent.addChild(transitionInstance);

    return transitionInstance;
  }

  @Override
  public void subscribeToEventsInScope() {
    // no event subscriptions for non-scope activities
  }

  public void cancel()
  {
    remove();
  }

}
