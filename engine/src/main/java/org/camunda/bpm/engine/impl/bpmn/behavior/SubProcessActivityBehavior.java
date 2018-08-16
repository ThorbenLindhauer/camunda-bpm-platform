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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.hackdays.ActivityInstance;
import org.camunda.bpm.engine.impl.hackdays.EventLoop;
import org.camunda.bpm.engine.impl.hackdays.IncomingTransitionInstance;
import org.camunda.bpm.engine.impl.hackdays.ScopeActivityInstance;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


/**
 * Implementation of the BPMN 2.0 subprocess (formally known as 'embedded' subprocess):
 * a subprocess defined within another process definition.
 *
 * @author Joram Barrez
 */
public class SubProcessActivityBehavior extends AbstractBpmnActivityBehavior implements CompositeActivityBehavior {

  @Override
  public void execute(ActivityInstance activityInstance) throws Exception {
    PvmActivity activity = activityInstance.getActivity();
    ActivityImpl initialActivity = activity.getProperties().get(BpmnProperties.INITIAL_ACTIVITY);
    IncomingTransitionInstance transitionInstance = ((ScopeActivityInstance) activityInstance).newIncomingTransitionInstance(initialActivity);
    EventLoop.run(transitionInstance);
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    PvmActivity activity = execution.getActivity();
    PvmActivity initialActivity = activity.getProperties().get(BpmnProperties.INITIAL_ACTIVITY);

    ensureNotNull("No initial activity found for subprocess " + execution.getActivity().getId(), "initialActivity", initialActivity);

    execution.executeActivity(initialActivity);
  }

  @Override
  public void concurrentChildExecutionEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {
    // join
    endedExecution.remove();
    scopeExecution.tryPruneLastConcurrentChild();
    scopeExecution.forceUpdate();
  }

  @Override
  public void complete(ActivityExecution scopeExecution) {
    leave(scopeExecution);
  }

  @Override
  public void doLeave(ActivityExecution execution) {
    CompensationUtil.createEventScopeExecution((ExecutionEntity) execution);

    super.doLeave(execution);
  }

}
