package com.jorhetfield.atelier.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class TtlCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val timeSource: TimeSource,
) {
    private class Entry<V>(
        val value: V,
        val storedAt: TimeMark,
    )

    private val mutex = Mutex()
    private val entries = HashMap<K, Entry<V>>()

    suspend fun get(key: K): V? =
        mutex.withLock {
            val entry = entries[key] ?: return@withLock null
            if (entry.isExpired()) {
                entries.remove(key)
                null
            } else {
                entry.value
            }
        }

    suspend fun put(
        key: K,
        value: V,
    ) = putAll(mapOf(key to value))

    suspend fun putAll(values: Map<K, V>) =
        mutex.withLock {
            entries.values.removeAll { it.isExpired() }
            val now = timeSource.markNow()
            values.forEach { (key, value) -> entries[key] = Entry(value, now) }
        }

    private fun Entry<V>.isExpired() = storedAt.elapsedNow() >= ttl
}
