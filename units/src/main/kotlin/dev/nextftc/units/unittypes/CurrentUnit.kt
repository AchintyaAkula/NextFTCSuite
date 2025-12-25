/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("CurrentUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Current

/**
 * Unit of measurement for electrical current.
 *
 * Supported units include amperes (base unit), milliamperes, microamperes, and kiloamperes.
 */
class CurrentUnit(
    baseUnit: CurrentUnit?,
    toBaseConverter: (Double) -> Double,
    fromBaseConverter: (Double) -> Double,
    unitName: String,
    unitSymbol: String,
) : Unit<CurrentUnit>(
    baseUnit,
    toBaseConverter,
    fromBaseConverter,
    unitName,
    unitSymbol,
) {
    internal constructor(
        baseUnit: CurrentUnit,
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

    override fun of(magnitude: Double): Current = Current(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): Measure<CurrentUnit> =
        of(this.fromBaseUnits(baseUnitMagnitude))
}

// Conversion constants
private const val MILLIAMPERES_PER_AMPERE = 1e-3
private const val MICROAMPERES_PER_AMPERE = 1e-6
private const val KILOAMPERES_PER_AMPERE = 1e3

// Current units
val Amperes = CurrentUnit(null, { it }, { it }, "ampere", "A")
val Milliamperes = CurrentUnit(Amperes, MILLIAMPERES_PER_AMPERE, "milliampere", "mA")
val Microamperes = CurrentUnit(Amperes, MICROAMPERES_PER_AMPERE, "microampere", "µA")
val Kiloamperes = CurrentUnit(Amperes, KILOAMPERES_PER_AMPERE, "kiloampere", "kA")

// Extension properties for Double
inline val Double.amperes get() = Amperes.of(this)
inline val Double.milliamperes get() = Milliamperes.of(this)
inline val Double.microamperes get() = Microamperes.of(this)
inline val Double.kiloamperes get() = Kiloamperes.of(this)
