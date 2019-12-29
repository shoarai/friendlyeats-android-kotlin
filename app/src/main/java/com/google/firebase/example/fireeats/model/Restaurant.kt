/**
 * Copyright 2017 Google Inc. All Rights Reserved.
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
package com.google.firebase.example.fireeats.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
class Restaurant {
    var name: String? = null
    var city: String? = null
    var category: String? = null
    var photo: String? = null
    var price = 0
    var numRatings = 0
    var avgRating = 0.0

    constructor()

    constructor(name: String?, city: String?, category: String?, photo: String?,
                price: Int, numRatings: Int, avgRating: Double) {
        this.name = name
        this.city = city
        this.category = category
        this.photo = photo
        this.price = price
        this.numRatings = numRatings
        this.avgRating = avgRating
    }

    companion object {
        const val FIELD_CITY = "city"
        const val FIELD_CATEGORY = "category"
        const val FIELD_PRICE = "price"
        const val FIELD_POPULARITY = "numRatings"
        const val FIELD_AVG_RATING = "avgRating"
    }
}