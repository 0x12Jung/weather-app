package com.opnt.takehometest.core.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class FakeDataStore<T>(initial: T) : DataStore<T> {
    private val state = MutableStateFlow(initial)
    override val data = state
    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        state.update { runBlocking { transform(it) } }
        return state.value
    }
}

private fun <R> runBlocking(block: suspend () -> R): R =
    kotlinx.coroutines.runBlocking { block() }
