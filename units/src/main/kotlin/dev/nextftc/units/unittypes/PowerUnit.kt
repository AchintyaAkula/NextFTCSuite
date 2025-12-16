/*
 * Copyright (c)  NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("PowerUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Power

/**
 * Unit of measurement for power (energy / time).
 *
 * Supported units include watts (base unit), milliwatts, kilowatts, megawatts, and horsepower.
 * Power is dimensionally equivalent to energy / time = mass × distance² / time³.
 */
class PowerUnit(
    baseUnit: PowerUnit?,
    toBaseConverter: (Double) -> Double,
    fromBaseConverter: (Double) -> Double,
    unitName: String,
    unitSymbol: String,
) : Unit<PowerUnit>(
    baseUnit,
    toBaseConverter,
    fromBaseConverter,
    unitName,
    unitSymbol,
) {
    internal constructor(
        baseUnit: PowerUnit,
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

    override fun of(magnitude: Double): Power = Power(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): Measure<PowerUnit> =
        of(this.fromBaseUnits(baseUnitMagnitude))

    override fun per(time: TimeUnit): Unit<*> {
        TODO("Power rate units not yet implemented")
    }
}

// Conversion constants
private const val MILLIWATTS_PER_WATT = 1e-3
private const val KILOWATTS_PER_WATT = 1e3
private const val MEGAWATTS_PER_WATT = 1e6
private const val HORSEPOWER_PER_WATT = 745.699872 // Mechanical horsepower

// Power units
val Watts = PowerUnit(null, { it }, { it }, "watt", "W")
val Milliwatts = PowerUnit(Watts, MILLIWATTS_PER_WATT, "milliwatt", "mW")
val Kilowatts = PowerUnit(Watts, KILOWATTS_PER_WATT, "kilowatt", "kW")
val Megawatts = PowerUnit(Watts, MEGAWATTS_PER_WATT, "megawatt", "MW")
val Horsepower = PowerUnit(Watts, HORSEPOWER_PER_WATT, "horsepower", "hp")

// Extension properties for Double
inline val Double.watts get() = Watts.of(this)
inline val Double.milliwatts get() = Milliwatts.of(this)
inline val Double.kilowatts get() = Kilowatts.of(this)
inline val Double.megawatts get() = Megawatts.of(this)
inline val Double.horsepower get() = Horsepower.of(this)
