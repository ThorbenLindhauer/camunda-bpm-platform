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

/**
 * @author Thorben Lindhauer
 *
 */
public class TakeSequenceFlowWorker implements TransitionInstanceWorker {

  @Override
  public TransitionInstanceState getHandledState() {
    return TransitionInstanceState.AFTER_ACTIVITY;
  }

  @Override
  public void handle(TransitionInstance transitionInstance, EventLoop eventLoop) {
    // 1. find target activity
    // 2. destroy transition instance
    // 3. create transition instance BEFORE_ACTIVITY and submit to event loop

  }

}
