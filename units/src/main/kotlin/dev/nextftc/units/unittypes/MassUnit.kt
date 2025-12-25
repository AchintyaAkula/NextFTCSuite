/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("MassUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Mass

/**
 * Unit of measurement for mass.
 *
 * Supported units include kilograms (base unit), grams, milligrams, metric tons, pounds, and
 * ounces.
 */
class MassUnit(
    baseUnit: MassUnit?,
    toBaseConverter: (Double) -> Double,
    fromBaseConverter: (Double) -> Double,
    unitName: String,
    unitSymbol: String,
) : Unit<MassUnit>(
    baseUnit,
    toBaseConverter,
    fromBaseConverter,
    unitName,
    unitSymbol,
) {
    internal constructor(
        baseUnit: MassUnit,
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

    override fun of(magnitude: Double): Mass = Mass(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): Measure<MassUnit> =
        of(this.fromBaseUnits(baseUnitMagnitude))
}

// Conversion constants
private const val GRAMS_PER_KILOGRAM = 1e-3
private const val MILLIGRAMS_PER_KILOGRAM = 1e-6
private const val METRIC_TONS_PER_KILOGRAM = 1000.0
private const val POUNDS_PER_KILOGRAM = 0.45359237
private const val OUNCES_PER_POUND = 1.0 / 16.0

// Metric units
val Kilograms = MassUnit(null, { it }, { it }, "kilogram", "kg")
val Grams = MassUnit(Kilograms, GRAMS_PER_KILOGRAM, "gram", "g")
val Milligrams = MassUnit(Kilograms, MILLIGRAMS_PER_KILOGRAM, "milligram", "mg")
val MetricTons = MassUnit(Kilograms, METRIC_TONS_PER_KILOGRAM, "metric ton", "t")

// Imperial units
val Pounds = MassUnit(Kilograms, POUNDS_PER_KILOGRAM, "pound", "lb")
val Ounces = MassUnit(Pounds, OUNCES_PER_POUND, "ounce", "oz")

// Extension properties for Double
inline val Double.kilograms get() = Kilograms.of(this)
inline val Double.grams get() = Grams.of(this)
inline val Double.milligrams get() = Milligrams.of(this)
inline val Double.metricTons get() = MetricTons.of(this)
inline val Double.pounds get() = Pounds.of(this)
inline val Double.ounces get() = Ounces.of(this)
