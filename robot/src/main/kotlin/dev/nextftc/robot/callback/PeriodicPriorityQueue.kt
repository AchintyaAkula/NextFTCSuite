/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot.callback

import java.util.PriorityQueue
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * A priority queue for scheduling periodic callbacks based on their next execution time.
 *
 * This class manages a collection of periodic callbacks that execute at specified intervals.
 * Callbacks are scheduled using monotonic timestamps and automatically rescheduled after execution
 * to maintain their periodic behavior. The queue uses a priority heap to efficiently determine the
 * next callback to execute.
 *
 * This is an internal scheduling primitive used by robot frameworks like TimedRobot.
 */
class PeriodicPriorityQueue {

  private val queue = PriorityQueue<PeriodicCallback>()
  private val timeSource = TimeSource.Monotonic
  private var loopStartTime: TimeSource.Monotonic.ValueTimeMark = timeSource.markNow()

  /**
   * Adds a periodic callback to the queue with a specified start time.
   *
   * @param func The function to call periodically.
   * @param period The callback period as a Duration.
   * @param offset The offset from the current monotonic time as a Duration.
   * @return the callback object.
   */
  fun add(func: () -> Unit, period: Duration, offset: Duration = Duration.ZERO): PeriodicCallback {
    val callback = PeriodicCallback(func, period, offset, timeSource)
    queue.add(callback)
    return callback
  }

  /**
   * Adds a periodic callback to the queue with a specified start time.
   *
   * @param func The function to call periodically.
   * @param period The callback period as a Duration.
   * @return the callback object.
   */
  fun add(func: () -> Unit, period: Duration): PeriodicCallback = add(func, period, Duration.ZERO)

  /**
   * Adds a pre-constructed callback to the queue.
   *
   * @param callback The callback to add.
   */
  fun add(callback: PeriodicCallback) {
    queue.add(callback)
  }

  /**
   * Adds multiple callbacks to the queue.
   *
   * @param callbacks The collection of callbacks to add.
   */
  fun addAll(callbacks: Collection<PeriodicCallback>) {
    queue.addAll(callbacks)
  }

  /**
   * Removes all callbacks associated with the given function.
   *
   * @param func The function whose callbacks should be removed.
   */
  fun remove(func: () -> Unit) {
    queue.removeIf { it.func == func }
  }

  /**
   * Removes a specific callback from the queue.
   *
   * @param callback The callback to remove.
   */
  fun remove(callback: PeriodicCallback) {
    queue.remove(callback)
  }

  /**
   * Removes multiple callbacks from the queue.
   *
   * @param callbacks The collection of callbacks to remove.
   */
  fun removeAll(callbacks: Collection<PeriodicCallback>) {
    queue.removeAll(callbacks.toSet())
  }

  /** Removes all callbacks from the queue. */
  fun clear() {
    queue.clear()
  }

  /**
   * Executes all callbacks that are due, then waits for the next callback's scheduled time.
   *
   * This method automatically advances callbacks' expiration times as necessary
   * to ensure they maintain proper timing even if execution runs behind.
   *
   * @param notifier The HAL notifier handle to use for timing.
   * @return whether the notifier was signaled before the timeout.
   * @throws IllegalStateException if the queue is empty when this method is called.
   */
  fun runCallbacks(notifier: Notifier): Boolean {
    val callback = queue.poll() ?: throw IllegalStateException(
      "No callbacks to run! Did you make sure to call add() first?",
    )

    notifier.setAlarm(callback.expirationTime, Duration.ZERO, true)

    try {
      notifier.waitForObject()
    } catch (ex: InterruptedException) {
      return false
    }

    loopStartTime = timeSource.markNow()
    callback.func()

    // Adjust expiration time to align with missed execution periods
    callback.advanceNextExecution(loopStartTime)
    queue.add(callback)

    // Process any other callbacks that are ready to run
    while (queue.peek()?.let { it.expirationTime <= loopStartTime.elapsedNow() } == true) {
      val nextCallback = queue.poll()
      nextCallback?.func()
      nextCallback?.let {
        it.advanceNextExecution(loopStartTime)
        queue.add(it)
      }
    }

    return true
  }

}
