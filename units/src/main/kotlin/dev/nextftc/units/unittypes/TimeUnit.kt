/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("TimeUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Time
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TimeUnit(
    baseUnit: TimeUnit?,
    toBaseConverter: (Double) -> Double,
    fromBaseConverter: (Double) -> Double,
    unitName: String,
    unitSymbol: String,
) : Unit<TimeUnit>(baseUnit, toBaseConverter, fromBaseConverter, unitName, unitSymbol) {
    internal constructor(
        baseUnit: TimeUnit,
        baseUnitEquivalent: Double,
        name: String,
        symbol: String,
    ) : this(
        baseUnit,
        { x -> x * baseUnitEquivalent },
        { x -> x / baseUnitEquivalent },
        name,
        symbol,
    )

    /**
     * Creates a new immutable measurement of the given magnitude in terms of this unit.
     * Implementations are **strongly** recommended to sharpen the return type to a unit-specific
     * measurement implementation.
     *
     * @param magnitude the magnitude of the measurement.
     * @return the measurement object
     */
    override fun of(magnitude: Double): Time {
        require(this.durationUnit != null) {
            "Unit $this does not have a corresponding internal duration unit."
        }

        return Time(magnitude.toDuration(this.durationUnit!!), this)
    }

    /**
     * Creates a new immutable measurement of the given magnitude in terms of this unit's base unit.
     * Implementations are **strongly** recommended to sharpen the return type to a unit-specific
     * measurement implementation.
     *
     * @param baseUnitMagnitude the magnitude in terms of the base unit
     * @return the measurement object
     */
    override fun ofBaseUnits(baseUnitMagnitude: Double): Measure<TimeUnit> =
        of(this.fromBaseUnits(baseUnitMagnitude))
}

// Conversion constants
private const val MILLISECONDS_PER_SECOND = 1e-3
private const val MICROSECONDS_PER_SECOND = 1e-6
private const val NANOSECONDS_PER_SECOND = 1e-9
private const val MINUTES_PER_SECOND = 60.0
private const val HOURS_PER_MINUTE = 60.0
private const val DAYS_PER_HOUR = 24.0

val Seconds = TimeUnit(null, { it }, { it }, "second", "s")
val Milliseconds = TimeUnit(Seconds, MILLISECONDS_PER_SECOND, "millisecond", "ms")
val Microseconds = TimeUnit(Seconds, MICROSECONDS_PER_SECOND, "microsecond", "us")
val Nanoseconds = TimeUnit(Seconds, NANOSECONDS_PER_SECOND, "nanosecond", "ns")
val Minutes = TimeUnit(Seconds, MINUTES_PER_SECOND, "minute", "min")
val Hours = TimeUnit(Minutes, HOURS_PER_MINUTE, "hour", "h")
val Days = TimeUnit(Hours, DAYS_PER_HOUR, "day", "d")

// Extension properties for Double
inline val Double.seconds get() = Seconds.of(this)
inline val Double.milliseconds get() = Milliseconds.of(this)
inline val Double.microseconds get() = Microseconds.of(this)
inline val Double.nanoseconds get() = Nanoseconds.of(this)
inline val Double.minutes get() = Minutes.of(this)
inline val Double.hours get() = Hours.of(this)
inline val Double.days get() = Days.of(this)

val DurationUnit.timeUnit
    get() =
        when (this) {
            DurationUnit.NANOSECONDS -> Nanoseconds
            DurationUnit.MICROSECONDS -> Microseconds
            DurationUnit.MILLISECONDS -> Milliseconds
            DurationUnit.SECONDS -> Seconds
            DurationUnit.MINUTES -> Minutes
            DurationUnit.HOURS -> Hours
            DurationUnit.DAYS -> Days
        }

val TimeUnit.durationUnit
    get() =
        when (this) {
            Seconds -> DurationUnit.SECONDS
            Milliseconds -> DurationUnit.MILLISECONDS
            Microseconds -> DurationUnit.MICROSECONDS
            Nanoseconds -> DurationUnit.NANOSECONDS
            Minutes -> DurationUnit.MINUTES
            Hours -> DurationUnit.HOURS
            Days -> DurationUnit.DAYS
            else -> null
        }
