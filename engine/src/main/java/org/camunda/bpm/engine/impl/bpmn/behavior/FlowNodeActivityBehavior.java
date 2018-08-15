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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.hackdays.ActivityInstance;
import org.camunda.bpm.engine.impl.hackdays.ActivityInstanceState;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;


/**
 * Superclass for all 'connectable' BPMN 2.0 process elements: tasks, gateways and events.
 * This means that any subclass can be the source or target of a sequenceflow.
 *
 * Corresponds with the notion of the 'flownode' in BPMN 2.0.
 *
 * @author Joram Barrez
 */
public abstract class FlowNodeActivityBehavior implements SignallableActivityBehavior {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  protected BpmnActivityBehavior bpmnActivityBehavior = new BpmnActivityBehavior();

  /**
   * Default behaviour: just leave the activity with no extra functionality.
   */
  public void execute(ActivityExecution execution) throws Exception {
    leave(execution);
  }

  @Override
  public void execute(ActivityInstance activityInstance) throws Exception {
    leave(activityInstance);
  }

  public void leave(ActivityInstance activityInstance)
  {
    ExecutionEntity execution = activityInstance.getExecution();

    LOG.leavingActivity(execution.getActivity().getId());

    String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");

    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    boolean anyMatched = false;
    for (PvmTransition outgoingTransition : outgoingTransitions) {
      if (defaultSequenceFlow == null || !outgoingTransition.getId().equals(defaultSequenceFlow)) {
        Condition condition = (Condition) outgoingTransition.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
        if (condition == null || condition.evaluate(execution)) {
          activityInstance.takeTransition((TransitionImpl) outgoingTransition);
          anyMatched = true;
        }
      }
    }

    if (!anyMatched)
    {
      if (defaultSequenceFlow != null) {
        PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
        if (defaultTransition != null) {
          activityInstance.takeTransition((TransitionImpl) defaultTransition);
        } else {
          throw LOG.missingDefaultFlowException(execution.getActivity().getId(), defaultSequenceFlow);
        }
      }
    }

    activityInstance.setState(ActivityInstanceState.COMPLETED);
  }

  /**
   * Default way of leaving a BPMN 2.0 activity: evaluate the conditions on the
   * outgoing sequence flow and take those that evaluate to true.
   */
  public void leave(ActivityExecution execution) {
    ((ExecutionEntity) execution).dispatchDelayedEventsAndPerformOperation(PvmAtomicOperation.ACTIVITY_LEAVE);
  }

  public void doLeave(ActivityExecution execution) {
    bpmnActivityBehavior.performDefaultOutgoingBehavior(execution);
  }

  protected void leaveIgnoreConditions(ActivityExecution activityContext) {
    bpmnActivityBehavior.performIgnoreConditionsOutgoingBehavior(activityContext);
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    // concrete activity behaviors that do accept signals should override this method;

    throw LOG.unsupportedSignalException(execution.getActivity().getId());
  }
}
