/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot.callback

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Pure Kotlin notifier with HAL-like signaling semantics.
 *
 * Time base is monotonic, using Kotlin's TimeSource and Duration libraries.
 *
 * @param timeSource the TimeSource to use for scheduling (defaults to TimeSource.Monotonic)
 */
class Notifier(private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic) :
  AutoCloseable {
  private val lock = ReentrantLock()
  private val cond = lock.newCondition()
  private val epochMark = timeSource.markNow()

  @Volatile
  private var running = true

  // Duration.INFINITE means "no alarm scheduled"
  private var alarmTime: Duration = Duration.INFINITE
  private var interval: Duration = Duration.ZERO
  private var signaled: Boolean = false

  private val worker = thread(
    start = true,
    isDaemon = true,
    name = "Notifier",
  ) {
    runLoop()
  }

  /**
   * Blocks until this notifier is signaled.
   * Mirrors waitForObject-style behavior.
   */
  @Throws(InterruptedException::class)
  fun waitForObject() {
    lock.withLock {
      while (running && !signaled) {
        cond.await()
      }
      if (!running) {
        throw IllegalStateException("Notifier is closed")
      }
    }
  }

  /**
   * Schedules an alarm at an absolute time.
   *
   * @param alarmTime the absolute time when the alarm should fire
   * @param interval periodic interval; Duration.ZERO for one-shot
   * @param ack true => clear prior signal state before scheduling
   */
  fun setAlarm(
    alarmTime: ComparableTimeMark,
    interval: Duration = Duration.ZERO,
    ack: Boolean = true,
  ) {
    // Calculate duration from the passed time mark
    val alarmDuration = alarmTime.elapsedNow()

    lock.withLock {
      if (!running) return@withLock

      if (ack) {
        signaled = false
      }

      this.alarmTime = alarmDuration
      this.interval = interval.coerceAtLeast(Duration.ZERO)

      // Wake worker to recompute wait deadline.
      cond.signalAll()
    }
  }

  /**
   * Schedules an alarm after a delay.
   *
   * @param delay how long to wait before the alarm fires
   * @param interval periodic interval; Duration.ZERO for one-shot
   * @param ack true => clear prior signal state before scheduling
   */
  fun setAlarm(delay: Duration, interval: Duration = Duration.ZERO, ack: Boolean = true) {
    val alarmDuration = nowElapsed() + delay.coerceAtLeast(Duration.ZERO)
    val intervalDuration = interval.coerceAtLeast(Duration.ZERO)

    lock.withLock {
      if (!running) return@withLock

      if (ack) {
        signaled = false
      }

      this.alarmTime = alarmDuration
      this.interval = intervalDuration

      // Wake worker to recompute wait deadline.
      cond.signalAll()
    }
  }

  /**
   * Cancels future alarms.
   *
   * @param ack true => clear prior signal state
   */
  fun cancel(ack: Boolean = true) {
    lock.withLock {
      if (!running) return@withLock
      alarmTime = Duration.INFINITE
      interval = Duration.ZERO
      if (ack) {
        signaled = false
      }
      cond.signalAll()
    }
  }

  /**
   * Acknowledges the currently signaled alarm, allowing future signals.
   */
  fun acknowledge() {
    lock.withLock {
      if (!running) return@withLock
      signaled = false
      cond.signalAll()
    }
  }

  override fun close() {
    lock.withLock {
      if (!running) return
      running = false
      cond.signalAll()
    }
    worker.join()
  }

  private fun runLoop() {
    try {
      lock.withLock {
        while (running) {
          val now = nowElapsed()
          when {
            alarmTime == Duration.INFINITE -> cond.await()
            alarmTime > now -> {
              val waitDuration = alarmTime - now
              cond.awaitNanos(waitDuration.inWholeNanoseconds)
            }
            else -> {
              // Alarm is due. Advance schedule first.
              if (interval > Duration.ZERO) {
                alarmTime += interval
                // Skip missed intervals to keep phase behavior HAL-like.
                val cur = nowElapsed()
                if (cur >= alarmTime) {
                  val missed = ((cur - alarmTime) / interval) + 1L
                  alarmTime += interval * missed.toInt()
                }
              } else {
                alarmTime = Duration.INFINITE
              }

              // HAL-like gate: do not signal again until acknowledged.
              if (!signaled) {
                signaled = true
                cond.signalAll()
              }
            }
          }
        }
      }
    } catch (_: InterruptedException) {
      Thread.currentThread().interrupt()
    }
  }

  private fun nowElapsed(): Duration = epochMark.elapsedNow()
}
