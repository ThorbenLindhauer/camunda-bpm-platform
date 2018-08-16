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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class ScopeActivityInstance extends ActivityInstance {

  private final ExecutionEntity execution;
  private List<ElementInstance> children = new ArrayList<>();

  public ScopeActivityInstance(ProcessDefinitionImpl processDefinition) {
    super(null, null);
    this.execution = ExecutionEntity.createNewExecution();
    this.execution.setProcessInstance(execution);
    this.execution.setProcessDefinition(processDefinition);
  }

  /**
   * constructur for deserialization
   */
  public ScopeActivityInstance(ScopeActivityInstance parent, ExecutionEntity execution, ActivityImpl activity)
  {
    super(parent, activity);
    this.execution = execution;
    if (parent != null)
    {
      this.parent.children.add(this);
    }
  }

  public ScopeActivityInstance(ScopeActivityInstance parent, ActivityImpl activity, ExecutionEntity attachableExecution)
  {
    super(parent, activity);
    this.execution = attachableExecution.createExecution();
    this.execution.setActivity(activity);
    this.execution.enterActivityInstance();
  }

  public ExecutionEntity getExecution() {
    return execution;
  }

  public IncomingTransitionInstance newIncomingTransitionInstance(ActivityImpl activity)
  {
    IncomingTransitionInstance instance = new IncomingTransitionInstance(this, activity);
    children.add(instance);
    return instance;
  }

  public IncomingTransitionInstance newIncomingTransitionInstance(ExecutionEntity execution, ActivityImpl activity)
  {
    IncomingTransitionInstance instance = new IncomingTransitionInstance(this, execution, activity);
    children.add(instance);
    return instance;
  }

  public OutgoingTransitionInstance newOutgoingTransitionInstance(ActivityImpl activity, TransitionImpl transition)
  {
    OutgoingTransitionInstance instance = new OutgoingTransitionInstance(this, activity, transition);
    children.add(instance);
    return instance;
  }

  public OutgoingTransitionInstance newOutgoingTransitionInstance(ActivityImpl activity, ExecutionEntity execution, TransitionImpl transition)
  {
    OutgoingTransitionInstance instance = new OutgoingTransitionInstance(this, activity, transition);
    children.add(instance);
    return instance;
  }

  public void removeChild(ElementInstance elementInstance)
  {
    this.children.remove(elementInstance);
  }

  public ActivityInstance newActivityInstance(ActivityImpl activity)
  {
    final ExecutionEntity attachableExecution;
    if (execution.hasChildren())
    {
      attachableExecution = (ExecutionEntity) execution.createConcurrentExecution();
    }
    else
    {
      attachableExecution = execution;
    }

    // TODO: set own activity id to null
    // TODO: maange activity instance iDS

    final ActivityInstance instance;
    if (activity.isScope())
    {
      instance = new ScopeActivityInstance(this, activity, attachableExecution);
    }
    else
    {
      instance = new NonScopeActivityInstance(this, activity, attachableExecution);
    }

    children.add(instance);
    return instance;
  }

  public ActivityInstance newActivityInstance(ExecutionEntity execution, ActivityImpl activity)
  {

    final ActivityInstance instance;
    if (activity.isScope())
    {
      instance = new ScopeActivityInstance(this, execution, activity);
    }
    else
    {
      instance = new NonScopeActivityInstance(this, execution, activity);
    }

    children.add(instance);
    return instance;

  }

  public void remove()
  {
    if (parent != null)
    {
      parent.removeChild(this);
    }

    this.execution.leaveActivityInstance();
    this.execution.setActivity(null);
    this.execution.destroy();
    this.execution.remove();
  }

  public boolean hasChildren()
  {
    return children.isEmpty();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("activity instance at activity ");
    sb.append(activity != null ? activity.getId() : "<process definition>");
    sb.append(" in state ");
    sb.append(getState());
    return sb.toString();
  }
}
