package com.example.todoapp.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
fun Timestamp.toInstant(): Instant =
    Instant.ofEpochSecond(this.seconds, this.nanoseconds.toLong())