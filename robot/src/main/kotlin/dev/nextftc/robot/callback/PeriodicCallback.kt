/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot.callback

import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * A periodic callback with scheduling metadata.
 *
 * Each callback tracks its target function, period, and next expiration time.
 * After execution, the expiration time is automatically advanced by full periods
 * to maintain precise timing even when execution is delayed.
 */
class PeriodicCallback(
  val func: () -> Unit,
  val period: Duration,
  offset: Duration,
  timeSource: TimeSource.Monotonic,
) : Comparable<PeriodicCallback> {

  private val startTime: TimeSource.Monotonic.ValueTimeMark = timeSource.markNow()
  var expirationTime: Duration = offset +
    Duration.INFINITE // Will be calculated on first advancement

  val expirationTimeMicros: Long
    get() = (startTime.elapsedNow() + expirationTime).inWholeMicroseconds

  fun advanceNextExecution(loopStartTime: TimeSource.Monotonic.ValueTimeMark) {
    val elapsedSinceStart = loopStartTime.elapsedNow() - startTime.elapsedNow()
    val missedPeriods = (elapsedSinceStart / period).toLong().coerceAtLeast(1)
    expirationTime += period * missedPeriods.toInt()
  }

  override fun equals(other: Any?): Boolean =
    other is PeriodicCallback && expirationTime == other.expirationTime

  override fun hashCode(): Int = expirationTime.hashCode()

  override fun compareTo(other: PeriodicCallback): Int = expirationTime.compareTo(other.expirationTime)
}