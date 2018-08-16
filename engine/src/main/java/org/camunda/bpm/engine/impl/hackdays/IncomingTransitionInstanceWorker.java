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

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class IncomingTransitionInstanceWorker implements TransitionInstanceWorker {

  @Override
  public TransitionInstanceState getHandledState() {
    return TransitionInstanceState.AFTER_ASYNC;
  }

  @Override
  public void handle(TransitionInstance transitionInstance, EventLoop eventLoop) {

    ScopeActivityInstance scopeInstance = transitionInstance.getParent();
    ActivityImpl activity = transitionInstance.getActivity();

    // 1. destroy transition instance
    transitionInstance.remove();

    // 2. create activity instance
    ActivityInstance activityInstance = scopeInstance.newActivityInstance(activity);

    // 3. signal event loop
    eventLoop.submit(activityInstance);
  }

}
