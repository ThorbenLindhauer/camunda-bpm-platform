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

import org.camunda.bpm.engine.impl.cmd.GetActivityInstanceCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityInstanceGenerator {

  private ExecutionManager executionManager;
  private ActivityInstance targetActivityInstance;
  private ExecutionEntity targetExecution;

  public ActivityInstanceGenerator(CommandContext commandContext)
  {
    executionManager = commandContext.getExecutionManager();
  }

  public ActivityInstance buildActivityInstanceTree(ExecutionEntity execution)
  {
    this.targetExecution = execution;

    org.camunda.bpm.engine.runtime.ActivityInstance activityInstance = new GetActivityInstanceCmd(execution.getProcessInstanceId()).execute(Context.getCommandContext());

    ExecutionEntity processInstance = executionManager.findExecutionById(execution.getProcessInstanceId());

    ProcessDefinitionEntity processDefinition = processInstance.getProcessDefinition();
    ActivityImpl activity = processDefinition.findActivity(activityInstance.getActivityId());

    ScopeActivityInstance rootActivityInstance = new ScopeActivityInstance(null, processInstance, activity);

    createChildren(rootActivityInstance, processDefinition, activityInstance.getChildActivityInstances());

    return targetActivityInstance;
  }

  private void createChildren(ScopeActivityInstance scopeInstance, ProcessDefinitionEntity processDefinition,
      org.camunda.bpm.engine.runtime.ActivityInstance[] children)
  {
    for (org.camunda.bpm.engine.runtime.ActivityInstance child : children)
    {
      ActivityImpl activity = processDefinition.findActivity(child.getActivityId());
      String[] executionIds = child.getExecutionIds();

      ExecutionEntity execution = null;
      if (executionIds.length == 1)
      {
        execution = executionManager.findExecutionById(executionIds[0]);
      }
      else
      {
        for (String executionId : executionIds)
        {
          ExecutionEntity executionCandidate = executionManager.findExecutionById(executionId);

          if (executionCandidate.isScope())
          {
            execution = executionCandidate;
            break;
          }
        }
      }

      if (execution == null)
      {
        throw new RuntimeException("no execution for activity instance found");
      }

      ActivityInstance newActivityInstance = scopeInstance.newActivityInstance(execution, activity);

      if (execution == targetExecution)
      {
        targetActivityInstance = newActivityInstance;
      }

      if (newActivityInstance instanceof ScopeActivityInstance)
      {
        createChildren((ScopeActivityInstance) newActivityInstance, processDefinition, child.getChildActivityInstances());
      }
    }

  }
}
