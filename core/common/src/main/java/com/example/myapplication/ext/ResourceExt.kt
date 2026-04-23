package com.example.myapplication.ext

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

@ColorInt
fun @receiver:ColorRes Int.toColor(context: Context): Int = ContextCompat.getColor(context, this)

fun @receiver:StringRes Int.toStr(context: Context): String = context.getString(this)

fun @receiver:DimenRes Int.toDimen(context: Context): Float = context.resources.getDimension(this)

fun @receiver:DimenRes Int.toDimenPx(context: Context): Int = context.resources.getDimensionPixelSize(this)

fun Context.getDrawable2(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(this, id)

fun Activity.getDrawable2(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(this, id)

fun Context.getColor2(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Activity.getColor2(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)
