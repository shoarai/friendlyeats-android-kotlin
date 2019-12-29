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
package com.google.firebase.example.fireeats

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.example.fireeats.model.Rating
import kotlinx.android.synthetic.main.dialog_rating.*

/**
 * Dialog Fragment containing rating form.
 */
class RatingDialogFragment : DialogFragment(), View.OnClickListener {
    internal interface RatingListener {
        fun onRating(rating: Rating?)
    }

    private var mRatingListener: RatingListener? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_rating, container, false)
        v.findViewById<View>(R.id.restaurant_form_button).setOnClickListener(this)
        v.findViewById<View>(R.id.restaurant_form_cancel).setOnClickListener(this)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RatingListener) {
            mRatingListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.restaurant_form_button -> onSubmitClicked(v)
            R.id.restaurant_form_cancel -> onCancelClicked(v)
        }
    }

    fun onSubmitClicked(view: View?) {
        val rating = Rating(
                FirebaseAuth.getInstance().currentUser,
                restaurant_form_rating.rating.toDouble(),
                restaurant_form_text.text.toString())
        if (mRatingListener != null) {
            mRatingListener!!.onRating(rating)
        }
        dismiss()
    }

    fun onCancelClicked(view: View?) {
        dismiss()
    }

    companion object {
        const val TAG = "RatingDialog"
    }
}