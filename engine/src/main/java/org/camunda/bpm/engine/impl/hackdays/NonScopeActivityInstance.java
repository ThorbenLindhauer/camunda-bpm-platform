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
public class NonScopeActivityInstance extends ActivityInstance {

  private ExecutionEntity execution;

  public NonScopeActivityInstance(ScopeActivityInstance parent, ActivityImpl activity, ExecutionEntity attachableExecution) {
    super(parent, activity);
    this.execution = attachableExecution;
    this.execution.setActivity(activity);
    this.execution.enterActivityInstance();
  }

  /**
   * constructor for deserialization
   */
  public NonScopeActivityInstance(ScopeActivityInstance parent, ExecutionEntity execution, ActivityImpl activity)
  {
    super(parent, activity);
    this.execution = execution;
  }

  @Override
  public void remove() {
    // do nothing for the time being;
    // TODO should probably reset activity id, activityinstance id and other stuff
    this.execution.leaveActivityInstance();
    this.execution.setActivity(activity.getParentFlowScopeActivity());

  }

  @Override
  public ActivityInstance newActivityInstance(ActivityImpl activity) {
    throw new UnsupportedOperationException("a non-scope activity instance cannot have children");
  }

  @Override
  public ExecutionEntity getExecution() {
    return execution;
  }

}
