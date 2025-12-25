/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("LinearVelocityUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.measuretypes.LinearVelocity

/**
 * Unit representing linear velocity (distance per time).
 *
 * Common examples:
 * - Meters per second (m/s)
 * - Miles per hour (mph)
 * - Kilometers per hour (km/h)
 * - Feet per second (ft/s)
 *
 * @param distance the distance unit (numerator)
 * @param time the time unit (denominator)
 */
class LinearVelocityUnit(distance: DistanceUnit, time: TimeUnit) :
    PerUnit<DistanceUnit, TimeUnit>(distance, time) {
    override fun of(magnitude: Double): LinearVelocity = LinearVelocity(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): LinearVelocity =
        of(this.fromBaseUnits(baseUnitMagnitude))

    /**
     * Combines this velocity unit with a unit of time to create an acceleration unit.
     */
    override fun per(time: TimeUnit): LinearAccelerationUnit =
        of(this, time) as LinearAccelerationUnit
}

// Common linear velocity units
val MetersPerSecond = LinearVelocityUnit(Meters, Seconds)
val KilometersPerHour = LinearVelocityUnit(Kilometers, Hours)
val MilesPerHour = LinearVelocityUnit(Miles, Hours)
val FeetPerSecond = LinearVelocityUnit(Feet, Seconds)
val InchesPerSecond = LinearVelocityUnit(Inches, Seconds)

// Extension properties for Double
inline val Double.metersPerSecond get() = MetersPerSecond.of(this)
inline val Double.kilometersPerHour get() = KilometersPerHour.of(this)
inline val Double.milesPerHour get() = MilesPerHour.of(this)
inline val Double.feetPerSecond get() = FeetPerSecond.of(this)
inline val Double.inchesPerSecond get() = InchesPerSecond.of(this)
