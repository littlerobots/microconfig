/*
 * Copyright 2025 Little Robots
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.littlerobots.microconfig

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable data class Schedule(val from: Instant? = null, val until: Instant? = null)

internal fun Schedule?.matches(instant: Instant): Boolean {
  if (this == null) {
    return true
  }
  if (from != null && instant < from) {
    return false
  }
  if (until != null && instant >= until) {
    return false
  }
  if (from == null || until == null) {
    return true
  }
  return instant >= from && instant < until
}
