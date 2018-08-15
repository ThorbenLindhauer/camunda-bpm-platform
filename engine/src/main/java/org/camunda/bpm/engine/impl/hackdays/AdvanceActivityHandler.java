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

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class AdvanceActivityHandler implements ActivityInstanceWorker {

  @Override
  public ActivityInstanceState getHandledState() {
    return ActivityInstanceState.COMPLETED;
  }

  @Override
  public void handle(ActivityInstance activityInstance, EventLoop eventLoop) {
    ScopeActivityInstance scopeInstance = activityInstance.getParent();

    // 1. get outgoing sequence flows from activity instance
    List<TransitionImpl> transitionsToTake = activityInstance.getTransitionsToTake();
    activityInstance.remove();

    // 2. create one transition instance per sequence flow
    // TODO: nicht so toll ist hier die if-else-Konstruktion; vll ist das ins Behavior des jeweiligen Scopes/Aktivität kodierbar?
    if (!transitionsToTake.isEmpty())
    {
      transitionsToTake.forEach(t -> {
        OutgoingTransitionInstance transitionInstance = scopeInstance.newOutgoingTransitionInstance(activityInstance.getActivity(), t);
        // 3. submit to event loop
        eventLoop.submit(transitionInstance);
      });
    }
    else
    {
      if (scopeInstance != null)
      {
        OutgoingTransitionInstance transitionInstance = scopeInstance.newOutgoingTransitionInstance(activityInstance.getActivity(), null);
        eventLoop.submit(transitionInstance);
      }
      else
      {
        // process instance
        // do nothing else for the time being
      }
    }
  }

}