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
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
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
  }

  public ScopeActivityInstance(ScopeActivityInstance parent, ActivityImpl activity, ExecutionEntity attachableExecution)
  {
    super(parent, activity);
    this.execution = attachableExecution.createExecution();
    this.execution.setActivity(activity);
    this.execution.enterActivityInstance();
    // remember our activity instance id in parent execution, so our execution can be reused for non-scope activities
    // TODO: this is handled differently for scope activities that cannot have child activity instances => not consistent with old engine yet
    this.execution.getParent().setActivityInstanceId(execution.getActivityInstanceId());
  }

  public ExecutionEntity getExecution() {
    return execution;
  }

  /* (non-Javadoc)
   * @see org.camunda.bpm.engine.impl.hackdays.ActivityInstance#complete()
   */
  @Override
  public void complete() {
    super.complete();
    // in case we are a scope and our children have completed, we need to restore our activity id
    this.execution.setActivity(activity);
  }

  public IncomingTransitionInstance newIncomingTransitionInstance(ActivityImpl activity)
  {
    final ExecutionEntity attachableExecution = createAttachableExecution();
    IncomingTransitionInstance instance = new IncomingTransitionInstance(this, attachableExecution, activity);
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
    final ExecutionEntity attachableExecution = createAttachableExecution();
    OutgoingTransitionInstance instance = new OutgoingTransitionInstance(this, attachableExecution, activity, transition);
    children.add(instance);
    return instance;
  }

  public OutgoingTransitionInstance newOutgoingTransitionInstance(ActivityImpl activity, ExecutionEntity execution, TransitionImpl transition)
  {

    OutgoingTransitionInstance instance = new OutgoingTransitionInstance(this, execution, activity, transition);
    children.add(instance);
    return instance;
  }

  public void removeChild(ElementInstance elementInstance)
  {
    this.children.remove(elementInstance);
  }

  public void addChild(ElementInstance elementInstance)
  {
    this.children.add(elementInstance);
  }

  public ActivityInstance newActivityInstance(ActivityImpl activity)
  {
    final ExecutionEntity attachableExecution = createAttachableExecution();

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

  private ExecutionEntity createAttachableExecution() {
    final ExecutionEntity attachableExecution;
    if (!children.isEmpty())
    {
      attachableExecution = (ExecutionEntity) execution.createConcurrentExecution();
    }
    else
    {
      attachableExecution = execution;
    }
    return attachableExecution;
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
      instance = new NonScopeActivityInstance(this, activity, execution);
    }

    this.children.add(instance);

    return instance;
  }

  @Override
  public void cancel() {
    children.forEach(c -> {
      if (c instanceof ActivityInstance)
      {
        ((ActivityInstance) c).cancel();
      }
      else
      {
        ((TransitionInstance) c).remove();
      }
    });

    remove();
  }

  protected ExecutionEntity destroy() {


    this.execution.leaveActivityInstance();
    this.execution.setActivity(null);
    this.execution.destroy();
    this.execution.remove();

    if (parent != null)
    {
      parent.removeChild(this);

      ExecutionEntity parentExecution = execution.getParent();
      parentExecution.leaveActivityInstance();
      return parentExecution;
    }
    else
    {
      return null;
    }
  }

  public boolean hasChildren()
  {
    return !children.isEmpty();
  }

  public List<ElementInstance> getChildren() {
    return children;
  }

  public List<ActivityInstance> getChildActivityInstances()
  {
    return children.stream().filter(e -> e instanceof ActivityInstance)
        .map(e -> (ActivityInstance) e)
        .collect(Collectors.toList());
  }

  public void subscribeToEventsInScope()
  {
    Map<String, EventSubscriptionDeclaration> declarationsForScope = EventSubscriptionDeclaration.getDeclarationsForScope(activity);

    declarationsForScope.values().forEach(d -> {
      if(!d.isStartEvent()) {
        d.createSubscriptionForExecution(getExecution());
      }
    });
  }

  @Override
  public OutgoingTransitionInstance toOutgoingInstance(TransitionImpl transition) {
    ExecutionEntity attachableExecution = destroy();
    OutgoingTransitionInstance transitionInstance = new OutgoingTransitionInstance(parent, attachableExecution, activity, transition);
    parent.addChild(transitionInstance);

    return transitionInstance;
  }
}
