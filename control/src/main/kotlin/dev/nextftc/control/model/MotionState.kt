/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.control.model

import dev.nextftc.units.measuretypes.Distance
import dev.nextftc.units.measuretypes.LinearAcceleration
import dev.nextftc.units.measuretypes.LinearVelocity
import dev.nextftc.units.unittypes.inches
import dev.nextftc.units.unittypes.inchesPerSecond
import dev.nextftc.units.unittypes.inchesPerSecondSquared

/**
 * The state of an object.
 *
 * @property position The position of the profile.
 * @property velocity The velocity of the profile.
 * @property acceleration The acceleration of the profile.
 */
data class MotionState @JvmOverloads constructor(
    val position: Distance = 0.0.inches,
    val velocity: LinearVelocity = 0.0.inchesPerSecond,
    val acceleration: LinearAcceleration = 0.0.inchesPerSecondSquared,
) {

    /**
     * Creates a MotionState with the given position, velocity, and acceleration.
     *
     * @param position Position, in inches
     * @param velocity Velocity, in inches per second
     * @param acceleration Acceleration, in inches per second squared
     */
    constructor(
        position: Double = 0.0,
        velocity: Double = 0.0,
        acceleration: Double = 0.0,
    ) : this(
        position.inches,
        velocity.inchesPerSecond,
        acceleration.inchesPerSecondSquared,
    )

    companion object {
        val ZERO = MotionState(0.0, 0.0, 0.0)
    }
}
