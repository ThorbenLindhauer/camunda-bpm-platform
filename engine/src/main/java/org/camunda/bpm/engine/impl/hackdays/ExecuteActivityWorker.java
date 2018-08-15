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
public class ExecuteActivityWorker implements ActivityInstanceWorker {

  @Override
  public ActivityInstanceState getHandledState() {
    return ActivityInstanceState.ACTIVATED;
  }

  @Override
  public void handle(ActivityInstance activityInstance, EventLoop eventLoop) {
    // 1. call activity behavior

    // 2. inspect state
    // 2.1. if state == COMPLETED
    //           => destroy activity instance, submit new transition instance with AFTER_ACTIVITY

  }

}
