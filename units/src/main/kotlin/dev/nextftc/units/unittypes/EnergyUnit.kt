/*
 * Copyright (c)  NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("EnergyUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Energy

/**
 * Unit of measurement for energy (force × distance).
 *
 * Supported units include joules (base unit), kilojoules, watt-hours, kilowatt-hours. Energy is
 * dimensionally equivalent to force × distance = mass × distance² / time².
 *
 * Note: While energy and torque have the same dimensions, they represent different physical
 * quantities - energy is a scalar, torque is a vector (moment).
 */
class EnergyUnit(
    baseUnit: EnergyUnit?,
    toBaseConverter: (Double) -> Double,
    fromBaseConverter: (Double) -> Double,
    unitName: String,
    unitSymbol: String,
) : Unit<EnergyUnit>(
    baseUnit,
    toBaseConverter,
    fromBaseConverter,
    unitName,
    unitSymbol,
) {
    internal constructor(
        baseUnit: EnergyUnit,
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

    override fun of(magnitude: Double): Energy = Energy(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): Measure<EnergyUnit> =
        of(this.fromBaseUnits(baseUnitMagnitude))

    override fun per(time: TimeUnit): Unit<*> {
        // Energy per time = Power
        TODO("Should return PowerUnit")
    }
}

// Conversion constants
private const val KILOJOULES_PER_JOULE = 1e3
private const val WATT_HOURS_PER_JOULE = 3600.0 // 1 Wh = 3600 J
private const val KILOWATT_HOURS_PER_JOULE = 3_600_000.0 // 1 kWh = 3,600,000 J

// Energy units
val Joules = EnergyUnit(null, { it }, { it }, "joule", "J")
val Kilojoules = EnergyUnit(Joules, KILOJOULES_PER_JOULE, "kilojoule", "kJ")
val WattHours = EnergyUnit(Joules, WATT_HOURS_PER_JOULE, "watt-hour", "Wh")
val KilowattHours = EnergyUnit(Joules, KILOWATT_HOURS_PER_JOULE, "kilowatt-hour", "kWh")

// Extension properties for Double
inline val Double.joules get() = Joules.of(this)
inline val Double.kilojoules get() = Kilojoules.of(this)
inline val Double.wattHours get() = WattHours.of(this)
inline val Double.kilowattHours get() = KilowattHours.of(this)
