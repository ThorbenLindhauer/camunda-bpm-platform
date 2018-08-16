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

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class TransitionInstance implements ElementInstance {


  protected final ScopeActivityInstance parent;
  protected final ActivityImpl activity;

  protected final ExecutionEntity execution;
  private TransitionInstanceState state = TransitionInstanceState.BEFORE_ASYNC;

  public TransitionInstance(ScopeActivityInstance parent, ActivityImpl activity, ExecutionEntity execution)
  {
    this.parent = parent;
    this.activity = activity;
    this.execution = execution;
    this.execution.setActivityInstanceId(null);
  }

  abstract boolean isAsync();

  abstract void createJob();

  public TransitionInstanceState getState() {
    return state;
  }

  public ExecutionEntity getExecution()
  {
    ExecutionEntity replacingExecution = execution.resolveReplacedBy();

    return replacingExecution != null ? replacingExecution : execution;
  }

  public void setState(TransitionInstanceState state) {
    this.state = state;
  }

  public void remove()
  {
    ExecutionEntity attachableExecution = destroy();

    if (attachableExecution.isConcurrent())
    {
      ExecutionEntity scopeExecution = attachableExecution.getParent();
      attachableExecution.remove();
      scopeExecution.tryPruneLastConcurrentChild();
    }
  }

  protected ExecutionEntity destroy() {
    parent.removeChild(this);
    ExecutionEntity execution = getExecution();
    execution.setActivity(null);
    execution.setActivityInstanceId(execution.getParentActivityInstanceId());

    return execution;
  }

  public ActivityImpl getActivity() {
    return activity;
  }

  public ScopeActivityInstance getParent() {
    return parent;
  }

}
