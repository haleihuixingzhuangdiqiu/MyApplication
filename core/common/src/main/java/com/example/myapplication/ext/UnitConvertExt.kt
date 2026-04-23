package com.example.myapplication.ext

import android.content.Context
import android.util.TypedValue
import android.view.View

fun Context.dpToPx(value: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

fun Context.spToPx(value: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)

fun View.dpToPx(value: Float): Float = context.dpToPx(value)

fun View.spToPx(value: Float): Float = context.spToPx(value)

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

val View.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

val View.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

val Context.density: Float
    get() = resources.displayMetrics.density

val View.density: Float
    get() = resources.displayMetrics.density

fun Float.pxToDp(context: Context): Float = this / context.resources.displayMetrics.density

fun Int.pxToDp(context: Context): Float = toFloat().pxToDp(context)

fun Float.pxToSp(context: Context): Float = this / context.resources.displayMetrics.scaledDensity

fun Int.pxToSp(context: Context): Float = toFloat().pxToSp(context)
