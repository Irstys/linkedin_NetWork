package ru.netology.linkedin_network.view

import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.linkedin_network.R

fun ImageView.load(url: String?, vararg transforms: BitmapTransformation = emptyArray()) {
    if (url == null) {
        setImageResource(R.drawable.ic_avatar)
    } else {
        Glide.with(this)
            .load(url)
            .error(R.drawable.ic_broken_image_24)
            .timeout(10_000)
            .transform(*transforms)
            .into(this)
    }
}

fun ImageView.loadCircleCrop(url: String?, vararg transforms: BitmapTransformation = emptyArray()) =
    url.let { load(it, CircleCrop(), *transforms) }


fun Fragment.titleListUsersFragment(@StringRes id: Int) {
    (activity as AppCompatActivity).supportActionBar?.title = context?.getString(id)
}

fun ImageView.loadImage(url: String?, view: ImageView) =
    Glide.with(view)
        .load(url)
        .placeholder(R.drawable.ic_loading_24)
        .error(R.drawable.ic_baseline_error_outline_24)
        .timeout(10_000)
        .into(view)

