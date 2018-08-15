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
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityInstance implements ElementInstance {

  private final ActivityInstance parent;
  private final ActivityImpl activity;

  private final ExecutionEntity execution;
  private List<ElementInstance> children = new ArrayList<>();
  private ActivityInstanceState state = ActivityInstanceState.ACTIVATED;

  private List<TransitionImpl> transitionsToTake = new ArrayList<>();

  public ActivityInstance(ProcessDefinitionImpl processDefinition) {
    this.execution = ExecutionEntity.createNewExecution();
    this.execution.setProcessInstance(execution);
    this.parent = null;
    this.activity = null;
  }

  public ActivityInstance(ActivityInstance parent, ActivityImpl activity)
  {
    this.parent = parent;
    this.activity = activity;
    this.execution = parent.execution;
    this.execution.setActivity(activity);
  }

  public ExecutionEntity getExecution() {
    return execution;
  }

  public ActivityInstanceState getState() {
    return state;
  }

  public void setState(ActivityInstanceState state) {
    this.state = state;
  }

  public IncomingTransitionInstance newIncomingTransitionInstance(ActivityImpl activity)
  {
    IncomingTransitionInstance instance = new IncomingTransitionInstance(this, activity);
    children.add(instance);
    return instance;
  }

  public OutgoingTransitionInstance newOutgoingTransitionInstance(ActivityImpl activity, TransitionImpl transition)
  {
    OutgoingTransitionInstance instance = new OutgoingTransitionInstance(this, activity, transition);
    children.add(instance);
    return instance;
  }

  public ActivityInstance newActivityInstance(ActivityImpl activity)
  {
    ActivityInstance instance = new ActivityInstance(this, activity);
    children.add(instance);
    return instance;
  }

  public void removeChild(ElementInstance elementInstance)
  {
    this.children.remove(elementInstance);
  }

  public ActivityImpl getActivity() {
    return activity;
  }

  public void invokeBehavior() {
    try {
      activity.getActivityBehavior().execute(this);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PvmException("couldn't execute activity <" + activity.getProperty("type") + " id=\"" + activity.getId() + "\" ...>: " + e.getMessage(), e);
    }
  }

  public ActivityInstance getParent() {
    return parent;
  }

  public List<TransitionImpl> getTransitionsToTake() {
    return transitionsToTake;
  }

  public void takeTransition(TransitionImpl transition)
  {
    transitionsToTake.add(transition);
  }

  public void remove()
  {
    if (parent != null)
    {
      parent.removeChild(this);
    }

    this.execution.setActivity(null);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("activity instance at activity ");
    sb.append(activity != null ? activity.getId() : "<process definition>");
    sb.append(" in state ");
    sb.append(state);
    return sb.toString();
  }
}
