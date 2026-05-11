/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot

import dev.nextftc.robot.callback.Notifier
import kotlin.time.Duration

abstract class RobotBase(val period: Duration) {
    private val notifier = Notifier()

    val voltage =
}