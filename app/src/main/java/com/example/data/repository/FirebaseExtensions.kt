package com.example.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        cont.cancel()
    }
}

fun DatabaseReference.observeSnapshots(): Flow<DataSnapshot> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            trySend(snapshot)
        }

        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    }
    addValueEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

fun DataSnapshot.stringValue(vararg keys: String, default: String = ""): String {
    for (key in keys) {
        val child = child(key)
        if (child.exists() && child.value != null) {
            return child.value.toString()
        }
    }
    return default
}

fun DataSnapshot.doubleValue(vararg keys: String, default: Double = 0.0): Double {
    for (key in keys) {
        val value = child(key).value
        when (value) {
            is Number -> return value.toDouble()
            is String -> value.toDoubleOrNull()?.let { return it }
        }
    }
    return default
}

fun DataSnapshot.longValue(vararg keys: String, default: Long = 0L): Long {
    for (key in keys) {
        val value = child(key).value
        when (value) {
            is Number -> return value.toLong()
            is String -> value.toLongOrNull()?.let { return it }
        }
    }
    return default
}

fun DataSnapshot.boolValue(vararg keys: String, default: Boolean = false): Boolean {
    for (key in keys) {
        val value = child(key).value
        when (value) {
            is Boolean -> return value
            is String -> return value.equals("true", ignoreCase = true)
            is Number -> return value.toInt() != 0
        }
    }
    return default
}
