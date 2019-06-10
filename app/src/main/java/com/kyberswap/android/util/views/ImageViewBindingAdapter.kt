package com.kyberswap.android.util.views

import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.caverock.androidsvg.SVG
import com.kyberswap.android.R
import com.kyberswap.android.presentation.main.profile.LoginType
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import jdenticon.Jdenticon

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
        var resourceIcon: Int?
        try {

            resourceIcon = view.context.resources.getIdentifier(
                identifier.toLowerCase(),
                "drawable",
                view.context.packageName
            )

            if (identifier.toLowerCase() == view.context.getString(R.string.token_eth_star).toLowerCase()) {
                resourceIcon = R.drawable.eth
            }

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

    @BindingAdapter("app:ratePercentage", "app:hasSamePair", "app:warning")
    @JvmStatic
    fun percentageRate(
        view: ImageView,
        percentageRate: String?,
        samePair: Boolean?,
        warning: Boolean?
    ) {

        if (samePair != null && samePair) {
            view.visibility = View.GONE
            return
        }

        if (percentageRate.toBigDecimalOrDefaultZero() > (-0.1).toBigDecimal()) {
            view.visibility = View.GONE
        } else {
            if (warning != null && warning) {
                view.visibility = View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    @BindingAdapter("app:tokenSymbol")
    @JvmStatic
    fun tokenSymbol(view: ImageView, tokenSymbol: String?) {
        if (tokenSymbol != null && tokenSymbol.toLowerCase() == view.context.getString(R.string.token_eth_star).toLowerCase()) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }

    }


    @BindingAdapter("app:address")
    @JvmStatic
    fun generateImage(view: ImageView, address: String?) {
        if (address.isNullOrEmpty()) return
        try {
            val svg = SVG.getFromString(
                Jdenticon.toSvg(
                    address.removePrefix("0x"),
                    view.layoutParams.width
                )
            )
            val drawable = PictureDrawable(svg.renderToPicture())
            Glide.with(view).load(drawable).into(view)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @BindingAdapter("app:loginType")
    @JvmStatic
    fun generateImage(view: ImageView, loginType: LoginType) {
        val drawable = when (loginType) {
            LoginType.GOOGLE -> {
                R.drawable.ic_google_plus_register
            }
            LoginType.FACEBOOK -> {
                R.drawable.ic_facebook_register
            }
            LoginType.TWITTER -> {
                R.drawable.ic_twitter_register
            }
            LoginType.NORMAL -> 0
        }

        Glide.with(view).load(drawable).into(view)

    }
}