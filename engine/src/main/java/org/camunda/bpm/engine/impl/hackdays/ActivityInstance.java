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
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class ActivityInstance implements ElementInstance {

  protected final ScopeActivityInstance parent;
  protected final ActivityImpl activity;
  private ActivityInstanceState state = ActivityInstanceState.ACTIVATED;
  private List<TransitionImpl> transitionsToTake = new ArrayList<>();

  public ActivityInstance(ScopeActivityInstance parent, ActivityImpl activity)
  {
    this.parent = parent;
    this.activity = activity;
  }


  public ActivityInstanceState getState() {
    return state;
  }

  public void setState(ActivityInstanceState state) {
    this.state = state;
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

  public ScopeActivityInstance getParent() {
    return parent;
  }

  public List<TransitionImpl> getTransitionsToTake() {
    return transitionsToTake;
  }

  public void takeTransition(TransitionImpl transition)
  {
    transitionsToTake.add(transition);
  }

  public abstract void remove();

  public abstract ActivityInstance newActivityInstance(ActivityImpl activity);

  public abstract ExecutionEntity getExecution();

}
