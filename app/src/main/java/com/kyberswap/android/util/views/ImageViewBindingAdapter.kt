package com.kyberswap.android.util.views

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kyberswap.android.R
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.tokenDrawableList

object ImageViewBindingAdapter {
    @BindingAdapter("app:imageUrl")
    @JvmStatic
    fun loadImage(view: ImageView, url: String?) {
        if (url.isNullOrEmpty()) return
        Glide.with(view).load(url).into(view)
    }

    @BindingAdapter("app:imageUrl", "app:error")
    @JvmStatic
    fun loadImage(view: ImageView, url: String?, error: Drawable) {
        if (url == null || url.isEmpty()) {
            view.setImageDrawable(error)
            return
        }
        Glide.with(view).load(url).apply(RequestOptions().error(error)).into(view)
    }

    @BindingAdapter("imageUrl", "placeHolder")
    @JvmStatic
    fun loadImageWithPlaceHolder(view: ImageView, url: String?, placeHolder: Drawable) {
        if (url == null || url.isEmpty()) {
            view.setImageDrawable(placeHolder)
            return
        }
        Glide.with(view).load(url).apply(RequestOptions().placeholder(placeHolder)).into(view)
    }

    @BindingAdapter("app:imageUri", "app:error")
    @JvmStatic
    fun loadImageUri(view: ImageView, uri: Uri?, error: Drawable) {
        if (uri == null) {
            view.setImageDrawable(error)
            return
        }
        Glide.with(view).load(uri).apply(RequestOptions().error(error)).into(view)
    }

    @BindingAdapter("app:resourceId")
    @JvmStatic
    fun loadResource(view: ImageView, resourceId: Int) {
        Glide.with(view).load("").error(resourceId).into(view)
    }

    @BindingAdapter("app:identifier")
    @JvmStatic
    fun loadResource(view: ImageView, identifier: String?) {
        if (identifier == null) return
        val resourceIcon: Int?
        try {

            resourceIcon = view.context.resources.getIdentifier(
                identifier.toLowerCase(),
                "drawable",
                view.context.packageName
            )
            if (resourceIcon == 0) {
                Glide.with(view).load(R.drawable.token_default).error(R.drawable.token_default)
                    .into(view)
            } else {
                Glide.with(view).load(resourceIcon).error(R.drawable.token_default).into(view)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }


    }

    @BindingAdapter("app:tokenSymbol")
    @JvmStatic
    fun loadTokenResource(view: ImageView, tokenSymbol: String) {
        var tokenDrawable = tokenDrawableList[tokenSymbol]
        if (tokenDrawable == null) {
            tokenDrawable = R.drawable.token_default
        }
        Glide.with(view).load(
            ""
        ).error(tokenDrawable).into(view)


    }

    @BindingAdapter("app:percentageRate")
    @JvmStatic
    fun percentageRate(view: ImageView, percentageRate: String?) {
        if (percentageRate.toBigDecimalOrDefaultZero() > (-0.1).toBigDecimal()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }

    }
}