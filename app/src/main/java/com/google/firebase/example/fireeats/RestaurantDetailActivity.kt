/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.example.fireeats

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.example.fireeats.RatingDialogFragment.RatingListener
import com.google.firebase.example.fireeats.adapter.RatingAdapter
import com.google.firebase.example.fireeats.model.Rating
import com.google.firebase.example.fireeats.model.Restaurant
import com.google.firebase.example.fireeats.util.RestaurantUtil.getPriceString
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_restaurant_detail.*

class RestaurantDetailActivity : AppCompatActivity(), View.OnClickListener, EventListener<DocumentSnapshot>, RatingListener {
    private lateinit var mRatingDialog: RatingDialogFragment
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mRestaurantRef: DocumentReference
    private lateinit var mRatingAdapter: RatingAdapter
    private var mRestaurantRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)
        restaurant_button_back.setOnClickListener(this)
        fab_show_rating_dialog.setOnClickListener(this)
        // Get restaurant ID from extras
        val restaurantId = intent.extras!!.getString(KEY_RESTAURANT_ID)
                ?: throw IllegalArgumentException("Must pass extra $KEY_RESTAURANT_ID")
        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance()
        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId)
        // Get ratings
        val ratingsQuery = mRestaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
        // RecyclerView
        mRatingAdapter = object : RatingAdapter(ratingsQuery) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    recycler_ratings.visibility = View.GONE
                    view_empty_ratings.visibility = View.VISIBLE
                } else {
                    recycler_ratings.visibility = View.VISIBLE
                    view_empty_ratings.visibility = View.GONE
                }
            }
        }
        recycler_ratings.layoutManager = LinearLayoutManager(this)
        recycler_ratings.adapter = mRatingAdapter
        mRatingDialog = RatingDialogFragment()
    }

    public override fun onStart() {
        super.onStart()
        mRatingAdapter.startListening()
        mRestaurantRegistration = mRestaurantRef.addSnapshotListener(this)
    }

    public override fun onStop() {
        super.onStop()
        mRatingAdapter.stopListening()
        if (mRestaurantRegistration != null) {
            mRestaurantRegistration!!.remove()
            mRestaurantRegistration = null
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.restaurant_button_back -> onBackArrowClicked(v)
            R.id.fab_show_rating_dialog -> onAddRatingClicked(v)
        }
    }

    private fun addRating(restaurantRef: DocumentReference?, rating: Rating?): Task<Void> {
        // Create reference for new rating, for use inside the transaction
        val ratingRef = restaurantRef!!.collection("ratings")
                .document()
        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction { transaction ->
            val restaurant = transaction[restaurantRef]
                    .toObject(Restaurant::class.java)
            // Compute new number of ratings
            val newNumRatings = restaurant!!.numRatings + 1
            // Compute new average rating
            val oldRatingTotal = restaurant.avgRating *
                    restaurant.numRatings
            val newAvgRating = (oldRatingTotal + rating!!.rating) /
                    newNumRatings
            // Set new restaurant info
            restaurant.numRatings = newNumRatings
            restaurant.avgRating = newAvgRating
            // Commit to Firestore
            transaction[restaurantRef] = restaurant
            transaction[ratingRef] = rating
            null
        }
    }

    /**
     * Listener for the Restaurant document ([.mRestaurantRef]).
     */
    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e)
            return
        }
        onRestaurantLoaded(snapshot!!.toObject(Restaurant::class.java))
    }

    private fun onRestaurantLoaded(restaurant: Restaurant?) {
        restaurant_name.text = restaurant!!.name
        restaurant_rating.rating = restaurant.avgRating.toFloat()
        restaurant_num_ratings.text = getString(R.string.fmt_num_ratings, restaurant.numRatings)
        restaurant_city.text = restaurant.city
        restaurant_category.text = restaurant.category
        restaurant_price.text = getPriceString(restaurant)
        // Background image
        Glide.with(restaurant_image.context)
                .load(restaurant.photo)
                .into(restaurant_image)
    }

    fun onBackArrowClicked(view: View?) {
        onBackPressed()
    }

    fun onAddRatingClicked(view: View?) {
        mRatingDialog.show(supportFragmentManager, RatingDialogFragment.TAG)
    }

    override fun onRating(rating: Rating?) { // In a transaction, add the new rating and update the aggregate totals
        addRating(mRestaurantRef, rating)
                .addOnSuccessListener(this) {
                    Log.d(TAG, "Rating added")
                    // Hide keyboard and scroll to top
                    hideKeyboard()
                    recycler_ratings.smoothScrollToPosition(0)
                }
                .addOnFailureListener(this) { e ->
                    Log.w(TAG, "Add rating failed", e)
                    // Show failure message and hide keyboard
                    hideKeyboard()
                    Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                            Snackbar.LENGTH_SHORT).show()
                }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {
        private const val TAG = "RestaurantDetail"
        const val KEY_RESTAURANT_ID = "key_restaurant_id"
    }
}