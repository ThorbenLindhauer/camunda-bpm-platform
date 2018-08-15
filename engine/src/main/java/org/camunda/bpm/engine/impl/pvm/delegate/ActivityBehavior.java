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
package org.camunda.bpm.engine.impl.pvm.delegate;

import org.camunda.bpm.engine.impl.core.delegate.CoreActivityBehavior;
import org.camunda.bpm.engine.impl.hackdays.ScopeActivityInstance;
import org.camunda.bpm.engine.impl.hackdays.ActivityInstance;
import org.camunda.bpm.engine.impl.hackdays.HackdaysActivityBehavior;


/**
 * @author Tom Baeyens
 */
public interface ActivityBehavior extends CoreActivityBehavior<ActivityExecution>, HackdaysActivityBehavior {

  void execute(ActivityExecution execution) throws Exception;

  @Override
  default public void execute(ActivityInstance activityInstance) throws Exception {
    execute(activityInstance.getExecution());
  };

}
