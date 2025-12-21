/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.control2.model

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Angle
import dev.nextftc.units.measuretypes.AngularAcceleration
import dev.nextftc.units.measuretypes.AngularVelocity
import dev.nextftc.units.measuretypes.Distance
import dev.nextftc.units.measuretypes.LinearAcceleration
import dev.nextftc.units.measuretypes.LinearVelocity
import dev.nextftc.units.measuretypes.Per
import dev.nextftc.units.unittypes.AngleUnit
import dev.nextftc.units.unittypes.DistanceUnit
import dev.nextftc.units.unittypes.PerUnit
import dev.nextftc.units.unittypes.Radians
import dev.nextftc.units.unittypes.RadiansPerSecond
import dev.nextftc.units.unittypes.RadiansPerSecondSquared
import dev.nextftc.units.unittypes.TimeUnit
import dev.nextftc.units.unittypes.inches
import dev.nextftc.units.unittypes.inchesPerSecond
import dev.nextftc.units.unittypes.inchesPerSecondSquared

/**
 * The kinematic state of a system, parameterized by position unit type.
 *
 * Velocity is derived as position per time, and acceleration as velocity per time.
 *
 * @param U The position unit type
 */
interface MotionState<U : Unit<U>> {
    /** The position of the system. */
    val position: Measure<U>

    /** The velocity of the system (position per time). */
    val velocity: Per<U, TimeUnit>

    /** The acceleration of the system (velocity per time). */
    val acceleration: Per<PerUnit<U, TimeUnit>, TimeUnit>

    fun copy(
        position: Measure<U> = this.position,
        velocity: Per<U, TimeUnit> = this.velocity,
        acceleration: Per<PerUnit<U, TimeUnit>, TimeUnit> = this.acceleration,
    ): MotionState<U>

    fun copy(
        position: Double = this.position.magnitude,
        velocity: Double = this.velocity.magnitude,
        acceleration: Double = this.acceleration.magnitude,
    ) = copy(
        this.position.unit.of(position),
        this.velocity.unit.of(velocity),
        this.acceleration.unit.of(acceleration),
    )
}

/**
 * The linear motion state of an object (position in distance units).
 *
 * @property position The position of the object.
 * @property velocity The velocity of the object.
 * @property acceleration The acceleration of the object.
 */
data class LinearMotionState @JvmOverloads constructor(
    override val position: Distance = 0.0.inches,
    override val velocity: LinearVelocity = 0.0.inchesPerSecond,
    override val acceleration: LinearAcceleration = 0.0.inchesPerSecondSquared,
) : MotionState<DistanceUnit> {

    /**
     * Creates a LinearMotionState with the given position, velocity, and acceleration.
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

    override fun copy(
        position: Measure<DistanceUnit>,
        velocity: Per<DistanceUnit, TimeUnit>,
        acceleration: Per<PerUnit<DistanceUnit, TimeUnit>, TimeUnit>,
    ): LinearMotionState = LinearMotionState(
        this.position.unit.of(position.into(this.position.unit)),
        this.velocity.unit.of(velocity.into(this.velocity.unit)) as LinearVelocity,
        this.acceleration.unit.of(acceleration.into(this.acceleration.unit)) as LinearAcceleration,
    )

    companion object {
        val ZERO = LinearMotionState(0.0, 0.0, 0.0)
    }
}

/**
 * The angular motion state of an object (position in angle units).
 *
 * @property position The angular position of the object.
 * @property velocity The angular velocity of the object.
 * @property acceleration The angular acceleration of the object.
 */
data class AngularMotionState @JvmOverloads constructor(
    override val position: Angle = Radians.of(0.0),
    override val velocity: AngularVelocity = RadiansPerSecond.of(0.0),
    override val acceleration: AngularAcceleration = RadiansPerSecondSquared.of(0.0),
) : MotionState<AngleUnit> {

    /**
     * Creates an AngularMotionState with the given position, velocity, and acceleration.
     *
     * @param position Position, in radians
     * @param velocity Velocity, in radians per second
     * @param acceleration Acceleration, in radians per second squared
     */
    constructor(
        position: Double = 0.0,
        velocity: Double = 0.0,
        acceleration: Double = 0.0,
    ) : this(
        Radians.of(position),
        RadiansPerSecond.of(velocity),
        RadiansPerSecondSquared.of(acceleration),
    )

    override fun copy(
        position: Measure<AngleUnit>,
        velocity: Per<AngleUnit, TimeUnit>,
        acceleration: Per<PerUnit<AngleUnit, TimeUnit>, TimeUnit>,
    ): AngularMotionState = AngularMotionState(
        this.position.unit.of(position.into(this.position.unit)),
        this.velocity.unit.of(velocity.into(this.velocity.unit)) as AngularVelocity,
        this.acceleration.unit.of(acceleration.into(this.acceleration.unit)) as AngularAcceleration,
    )

    companion object {
        val ZERO = AngularMotionState(0.0, 0.0, 0.0)
    }
}
