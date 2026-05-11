/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.nextftc.robot.callback.Notifier
import dev.nextftc.robot.callback.PeriodicPriorityQueue
import dev.nextftc.units.measuretypes.Time
import dev.nextftc.units.measuretypes.toDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class PeriodicOpMode(period: Duration) : LinearOpMode() {
    constructor(period: Time) : this(period.toDuration())
    constructor(period: Double) : this(period.seconds)

    private val notifier = Notifier()
    private val callbackQueue = PeriodicPriorityQueue()

    open fun initialize() = Unit

    open fun initLoop() = Unit

    open fun begin() = Unit

    open fun periodic() = Unit

    final override fun runOpMode() {
        initialize()

        while (opModeInInit()) {
            initLoop()
        }

        waitForStart()

        while (opModeIsActive()) {
            if (!callbackQueue.runCallbacks(notifier)) {
                return
            }
        }
    }
}