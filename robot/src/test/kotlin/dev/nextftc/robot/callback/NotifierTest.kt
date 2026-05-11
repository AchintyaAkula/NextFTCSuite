/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot.callback

import io.kotest.core.spec.style.FunSpec
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@OptIn(ExperimentalAtomicApi::class)
class NotifierTest :
  FunSpec({
    test("wait for object blocks until signaled") {
      val testTimeSource = TestTimeSource()
      val signalReceived = AtomicInt(0)

      val notifier = Notifier(testTimeSource)
      notifier.use { notifier ->
        // Schedule an alarm in 100ms
        val alarmMark = testTimeSource.markNow()
        notifier.setAlarm(alarmMark + 100.milliseconds, Duration.ZERO)

        // Advance time and the alarm should fire
        testTimeSource += 100.milliseconds

        // This should now succeed without blocking indefinitely
        notifier.waitForObject()
        signalReceived.incrementAndFetch()

        signalReceived.load() shouldBe 1
      }
    }

    test("periodic alarms fire at regular intervals") {
      val testTimeSource = TestTimeSource()
      val fireCount = AtomicInt(0)
      val notifier = Notifier(testTimeSource)

      notifier.use { notifier ->
        // Schedule a periodic alarm every 50ms
        val alarmMark = testTimeSource.markNow()
        notifier.setAlarm(alarmMark + 50.milliseconds, 50.milliseconds)

        // Advance time and fire first alarm
        testTimeSource += 50.milliseconds
        notifier.waitForObject()
        fireCount.incrementAndFetch()
        fireCount.load() shouldBe 1

        // Advance more and fire second alarm
        notifier.setAlarm(testTimeSource.markNow() + 50.milliseconds, 50.milliseconds)
        testTimeSource += 50.milliseconds
        notifier.waitForObject()
        fireCount.incrementAndFetch()
        fireCount.load() shouldBe 2
      }
    }

    test("cancel prevents alarm from firing") {
      val testTimeSource = TestTimeSource()
      val notifier = Notifier(testTimeSource)
      val waitReturned = AtomicInt(0)

      notifier.use { notifier ->
        val alarmMark = testTimeSource.markNow()
        notifier.setAlarm(alarmMark + 100.milliseconds, Duration.ZERO)

        val waiter = thread {
          try {
            notifier.waitForObject()
            waitReturned.incrementAndFetch()
          } catch (_: IllegalStateException) {
            // Expected if the notifier is closed while waiting.
          }
        }

        // Cancel before alarm fires
        notifier.cancel()

        // Advance time past when alarm would have fired
        testTimeSource += 150.milliseconds

        Thread.sleep(20)
        waitReturned.load() shouldBe 0
        waiter.interrupt()
        waiter.join()
      }
    }

    test("acknowledge allows subsequent signals") {
      val testTimeSource = TestTimeSource()
      val fireCount = AtomicInt(0)
      val notifier = Notifier(testTimeSource)

      notifier.use { notifier ->
        // First alarm
        val mark1 = testTimeSource.markNow()
        notifier.setAlarm(mark1 + 50.milliseconds, Duration.ZERO)
        testTimeSource += 50.milliseconds
        notifier.waitForObject()
        fireCount.incrementAndFetch()

        // Acknowledge to clear the signal
        notifier.acknowledge()

        // Second alarm after acknowledgement
        val mark2 = testTimeSource.markNow()
        notifier.setAlarm(mark2 + 50.milliseconds, Duration.ZERO)
        testTimeSource += 50.milliseconds
        notifier.waitForObject()
        fireCount.incrementAndFetch()

        fireCount.load() shouldBe 2
      }
    }

    test("absolute time scheduling with TimeSource mark") {
      val testTimeSource = TestTimeSource()
      val notifier = Notifier(testTimeSource)
      val signalReceived = AtomicInt(0)

      notifier.use { notifier ->
        // Create a mark for a future time
        val futureMark = testTimeSource.markNow() + 100.milliseconds
        notifier.setAlarm(futureMark, Duration.ZERO)

        // Advance to that time
        testTimeSource += 100.milliseconds

        // Should be able to wait for the object without hanging
        notifier.waitForObject()
        signalReceived.incrementAndFetch()
      }

      signalReceived.load() shouldBe 1
    }

    test("relative delay scheduling") {
      val testTimeSource = TestTimeSource()
      val notifier = Notifier(testTimeSource)
      val signalReceived = AtomicInt(0)

      notifier.use { notifier ->
        // Schedule with a relative delay of 100ms
        notifier.setAlarm(100.milliseconds, Duration.ZERO)

        // Advance by the delay
        testTimeSource += 100.milliseconds

        // Alarm should fire
        notifier.waitForObject()
        signalReceived.incrementAndFetch()
      }

      signalReceived.load() shouldBe 1
    }

    test("negative delay is coerced to zero and fires immediately") {
      val testTimeSource = TestTimeSource()
      val notifier = Notifier(testTimeSource)
      val signalReceived = AtomicInt(0)

      notifier.use { notifier ->
        // Schedule with negative delay (should be coerced to 0)
        notifier.setAlarm((-50).milliseconds, Duration.ZERO)

        // No need to advance time - should fire immediately
        testTimeSource += 0.milliseconds
        notifier.waitForObject()
        signalReceived.incrementAndFetch()
      }

      signalReceived.load() shouldBe 1
    }

    test("sequential alarms with cancel in between") {
      val testTimeSource = TestTimeSource()
      val fireCount = AtomicInt(0)
      val notifier = Notifier(testTimeSource)

      notifier.use { notifier ->
        // First alarm
        notifier.setAlarm(50.milliseconds, Duration.ZERO)
        testTimeSource += 50.milliseconds
        notifier.waitForObject()
        fireCount.incrementAndFetch()

        // Cancel and reschedule
        notifier.cancel()
        notifier.setAlarm(100.milliseconds, Duration.ZERO)
        testTimeSource += 100.milliseconds
        notifier.waitForObject()
        fireCount.incrementAndFetch()

        fireCount.load() shouldBe 2
      }
    }

    test("multiple acknowledgements are safe") {
      shouldNotThrowAny {
        val notifier = Notifier()
        notifier.use { notifier ->
          notifier.acknowledge()
          notifier.acknowledge()
          notifier.acknowledge()
        }
      }
    }

    test("close is idempotent") {
      shouldNotThrowAny {
        val testTimeSource = TestTimeSource()
        val notifier = Notifier(testTimeSource)
        notifier.close()

        // After close, operations should not crash
        notifier.cancel()
        notifier.acknowledge()
      }
    }
  })
