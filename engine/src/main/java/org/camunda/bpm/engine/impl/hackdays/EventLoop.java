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

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class EventLoop {

  private Map<ActivityInstanceState, ActivityInstanceWorker> activityInstanceWorkers = new HashMap<>();
  private Map<TransitionInstanceState, TransitionInstanceWorker> transitionInstanceWorkers = new HashMap<>();

  private Deque<Object> stuffToWorkOn = new LinkedList<>();



  public void submit(ActivityInstance activityInstance)
  {
    stuffToWorkOn.addFirst(activityInstance);
  }


  public void submit(TransitionInstance transitionInstance)
  {
    stuffToWorkOn.addFirst(transitionInstance);
  }


  public void doWork() {
    while (!stuffToWorkOn.isEmpty())
    {
      Object nextElement = stuffToWorkOn.removeFirst();

      if (nextElement instanceof ActivityInstance)
      {
        ActivityInstance activityInstance = (ActivityInstance) nextElement;
        ActivityInstanceWorker worker = activityInstanceWorkers.get(activityInstance.getState());
        worker.handle(activityInstance, this);
      }
      else
      {
        TransitionInstance transitionInstance = (TransitionInstance) nextElement;
        TransitionInstanceWorker worker = transitionInstanceWorkers.get(transitionInstance.getState());
        worker.handle(transitionInstance, this);
      }
    }
  }
}
