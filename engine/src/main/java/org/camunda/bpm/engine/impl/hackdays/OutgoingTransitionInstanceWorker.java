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

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class OutgoingTransitionInstanceWorker implements TransitionInstanceWorker {

  @Override
  public void handle(TransitionInstance transitionInstance, EventLoop eventLoop) {
    // TODO: this should not be required
    TransitionImpl transition = ((OutgoingTransitionInstance) transitionInstance).getTransition();
    ScopeActivityInstance scopeInstance = transitionInstance.getParent();

    if (transition != null)
    {
      // 1. find target activity
      PvmActivity destination = transition.getDestination();

      // 2. destroy transition instance
      transitionInstance.remove();

      // 3. create transition instance BEFORE_ACTIVITY and submit to event loop
      IncomingTransitionInstance incomingInstance = new IncomingTransitionInstance(scopeInstance, (ActivityImpl) destination);
      eventLoop.submit(incomingInstance);
    }
    else
    {
      transitionInstance.remove();

      scopeInstance.setState(ActivityInstanceState.COMPLETED);
      eventLoop.submit(scopeInstance);
    }
  }

  @Override
  public TransitionInstanceState getHandledState() {
    return TransitionInstanceState.AFTER_ASYNC;
  }
}
