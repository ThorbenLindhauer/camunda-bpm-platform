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
public class IncomingTransitionInstance implements ElementInstance {

  private final ActivityInstance parent;
  private final ActivityImpl activity;

  private final ExecutionEntity execution;

  public IncomingTransitionInstance(ActivityInstance parent, ActivityImpl activity)
  {
    this.parent = parent;
    this.activity = activity;
    this.execution = parent.getExecution();
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

  public ActivityInstance getParent() {
    return parent;
  }
}
