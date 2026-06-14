package com.wifiguard.app.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

fun Long.asDateTime(): String = dateFormat.format(Date(this))
