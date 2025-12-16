/*
 * Copyright (c)  NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("AngularVelocityUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.measuretypes.AngularVelocity

/**
 * Unit representing angular velocity (angle per time).
 *
 * Common examples:
 * - Radians per second (rad/s)
 * - Degrees per second (deg/s)
 * - Rotations per minute (RPM)
 * - Revolutions per second (Hz when measuring rotation frequency)
 *
 * @param angle the angle unit (numerator)
 * @param time the time unit (denominator)
 */
class AngularVelocityUnit(angle: AngleUnit, time: TimeUnit) :
    PerUnit<AngleUnit, TimeUnit>(angle, time) {
    override fun of(magnitude: Double): AngularVelocity = AngularVelocity(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): AngularVelocity =
        of(this.fromBaseUnits(baseUnitMagnitude))
}

// Common angular velocity units
val RadiansPerSecond = AngularVelocityUnit(Radians, Seconds)
val DegreesPerSecond = AngularVelocityUnit(Degrees, Seconds)
val RotationsPerMinute = AngularVelocityUnit(Rotations, Minutes)
val RotationsPerSecond = AngularVelocityUnit(Rotations, Seconds)

// Extension properties for Double
inline val Double.radiansPerSecond get() = RadiansPerSecond.of(this)
inline val Double.degreesPerSecond get() = DegreesPerSecond.of(this)
inline val Double.rotationsPerMinute get() = RotationsPerMinute.of(this)
inline val Double.rotationsPerSecond get() = RotationsPerSecond.of(this)
inline val Double.rpm get() = RotationsPerMinute.of(this)
