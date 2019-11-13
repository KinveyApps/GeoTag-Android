/**
 * Copyright (c) 2019 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.kinvey.sample.geotag

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * @author edwardf
 * @since 2.0
 */
data class GeoTagEntity(
    @Key("_id")
    var objectId: String? = null,
    @Key("note")
    var note: String? = null,
    @Key("_geoloc")
    var coords: List<Double>? = null
) : GenericJson()