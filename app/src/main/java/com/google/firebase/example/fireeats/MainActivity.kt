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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter.OnRestaurantSelectedListener
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.example.fireeats.viewmodel.MainActivityViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener, FilterDialogFragment.FilterListener, OnRestaurantSelectedListener {
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mQuery: Query
    private lateinit var mFilterDialog: FilterDialogFragment
    private lateinit var mAdapter: RestaurantAdapter
    private lateinit var mViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        filter_bar.setOnClickListener(this)
        button_clear_filter.setOnClickListener(this)
        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)
        // Initialize Firestore and the main RecyclerView
        initFirestore()
        initRecyclerView()
        // Filter Dialog
        mFilterDialog = FilterDialogFragment()
    }

    private fun initFirestore() {
        mFirestore = FirebaseFirestore.getInstance()
        // Get the 50 highest rated restaurants
        mQuery = mFirestore
                .collection("restaurants")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(LIMIT.toLong())
    }

    private fun initRecyclerView() {
        mAdapter = object : RestaurantAdapter(mQuery, this@MainActivity) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (itemCount == 0) {
                    recycler_restaurants.visibility = View.GONE
                    view_empty.visibility = View.VISIBLE
                } else {
                    recycler_restaurants.visibility = View.VISIBLE
                    view_empty.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException?) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show()
            }
        }
        recycler_restaurants.layoutManager = LinearLayoutManager(this)
        recycler_restaurants.adapter = mAdapter
    }

    public override fun onStart() {
        super.onStart()
        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn()
            return
        }
        // Apply filters
        onFilter(mViewModel.filters)
        // Start listening for Firestore updates
        mAdapter.startListening()
    }

    public override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    private fun onAddItemsClicked() {
        // Get a reference to the restaurants collection
        val restaurants = mFirestore.collection("restaurants")
        for (i in 0..9) { // Get a random Restaurant POJO
            val restaurant = RestaurantUtil.getRandom(this)
            // Add a new document to the restaurants collection
            restaurants.add(restaurant)
        }
    }

    override fun onFilter(filters: Filters) {
        // Construct query basic query
        var query: Query = mFirestore.collection("restaurants")
        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo("category", filters.category)
        }
        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo("city", filters.city)
        }
        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo("price", filters.price)
        }
        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            filters.sortBy?.let { sortBy ->
                filters.sortDirection?.let { sortDirection ->
                    query = query.orderBy(sortBy, sortDirection)
                }
            }
        }
        // Limit items
        query = query.limit(LIMIT.toLong())
        // Update the query
        mQuery = query
        mAdapter.setQuery(query)
        // Set header
        text_current_search.text = Html.fromHtml(filters.getSearchDescription(this))
        text_current_sort_by.text = filters.getOrderDescription(this)
        // Save filters
        mViewModel.filters = filters
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_items -> onAddItemsClicked()
            R.id.menu_sign_out -> {
                AuthUI.getInstance().signOut(this)
                startSignIn()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            mViewModel.isSigningIn = false
            if (resultCode != Activity.RESULT_OK && shouldStartSignIn()) {
                startSignIn()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.filter_bar -> onFilterClicked()
            R.id.button_clear_filter -> onClearFilterClicked()
        }
    }

    fun onFilterClicked() { // Show the dialog containing filter options
        mFilterDialog.show(supportFragmentManager, FilterDialogFragment.TAG)
    }

    fun onClearFilterClicked() {
        mFilterDialog.resetFilters()
        onFilter(Filters.default)
    }

    override fun onRestaurantSelected(restaurant: DocumentSnapshot) {
        // Go to the details page for the selected restaurant
        val intent = Intent(this, RestaurantDetailActivity::class.java)
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.id)
        startActivity(intent)
    }

    private fun shouldStartSignIn(): Boolean {
        return !mViewModel.isSigningIn && FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignIn() {
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
        )

        // Sign in with FirebaseUI
        val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()
        startActivityForResult(intent, RC_SIGN_IN)
        mViewModel.isSigningIn = true
    }

    private fun showTodoToast() {
        Toast.makeText(this, "TODO: Implement", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
        private const val LIMIT = 50
    }
}