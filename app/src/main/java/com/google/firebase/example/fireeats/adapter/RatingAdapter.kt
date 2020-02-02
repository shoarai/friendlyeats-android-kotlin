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
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.model.Rating
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_rating.view.*

/**
 * RecyclerView adapter for a bunch of Ratings.
 */
open class RatingAdapter(query: Query) : FirestoreAdapter<RatingAdapter.ViewHolder?>(query) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rating, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position).toObject(Rating::class.java))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(rating: Rating?) {
            itemView.rating_item_name.text = rating!!.userName
            itemView.rating_item_rating.rating = rating.rating.toFloat()
            itemView.rating_item_text.text = rating.text
        }
    }
}