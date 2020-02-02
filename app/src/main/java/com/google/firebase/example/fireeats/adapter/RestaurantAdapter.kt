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
package com.google.firebase.example.fireeats.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.model.Restaurant
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_restaurant.view.*

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class RestaurantAdapter(query: Query, private val mListener: OnRestaurantSelectedListener) :
        FirestoreAdapter<RestaurantAdapter.ViewHolder?>(query) {
    interface OnRestaurantSelectedListener {
        fun onRestaurantSelected(restaurant: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(snapshot: DocumentSnapshot, listener: OnRestaurantSelectedListener?) {
            snapshot.toObject(Restaurant::class.java)?.let { restaurant ->
                val resources = itemView.resources

                // Load image
                Glide.with(itemView.restaurant_item_image.context)
                        .load(restaurant.photo)
                        .into(itemView.restaurant_item_image)
                itemView.restaurant_item_name.text = restaurant.name
                itemView.restaurant_item_rating.rating = restaurant.avgRating.toFloat()
                itemView.restaurant_item_city.text = restaurant.city
                itemView.restaurant_item_category.text = restaurant.category
                itemView.restaurant_item_num_ratings.text = resources.getString(
                        R.string.fmt_num_ratings, restaurant.numRatings)
                itemView.restaurant_item_price.text = RestaurantUtil.getPriceString(restaurant)

                // Click listener
                itemView.setOnClickListener { listener?.onRestaurantSelected(snapshot) }
            }
        }
    }
}