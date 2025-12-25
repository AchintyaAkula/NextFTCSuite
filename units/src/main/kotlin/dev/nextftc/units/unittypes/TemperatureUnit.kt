/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("TemperatureUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Temperature

/**
 * Unit of measurement for temperature.
 *
 * Supported units include Celsius (base unit), Fahrenheit, and Kelvin. Note: Temperature
 * conversions handle both absolute values and temperature differences correctly.
 */
class TemperatureUnit(
    baseUnit: TemperatureUnit?,
    toBaseConverter: (Double) -> Double,
    fromBaseConverter: (Double) -> Double,
    unitName: String,
    unitSymbol: String,
) : Unit<TemperatureUnit>(
    baseUnit,
    toBaseConverter,
    fromBaseConverter,
    unitName,
    unitSymbol,
) {
    /**
     * Convenience constructor for derived temperature units that are a linear scaling of the base unit.
     *
     * NOTE: This constructor only supports multiplicative conversions (no offset). It is suitable
     * for units that scale linearly with the base unit (e.g., a hypothetical unit that's 1000× Celsius).
     * Do NOT use this constructor for offset-based units like Fahrenheit or Kelvin — those require
     * explicit converters (see `Fahrenheit` and `Kelvin` below).
     *
     * @param baseUnit the unit to derive from (typically `Celsius`)
     * @param baseUnitEquivalent how many base unit units equal one of this unit (multiplier)
     * @param name the human-readable name of the unit
     * @param symbol the short symbol used for the unit
     */
    constructor(
        baseUnit: TemperatureUnit,
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

    override fun of(magnitude: Double): Temperature = Temperature(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): Measure<TemperatureUnit> =
        of(this.fromBaseUnits(baseUnitMagnitude))
}

// Temperature units with conversion formulas
val Celsius = TemperatureUnit(null, { it }, { it }, "celsius", "°C")

// Fahrenheit: °C = (°F - 32) × 5/9
val Fahrenheit =
    TemperatureUnit(
        Celsius,
        { fahrenheit -> (fahrenheit - 32.0) * 5.0 / 9.0 },
        { celsius -> celsius * 9.0 / 5.0 + 32.0 },
        "fahrenheit",
        "°F",
    )

// Kelvin: °C = K - 273.15
val Kelvin =
    TemperatureUnit(
        Celsius,
        { kelvin -> kelvin - 273.15 },
        { celsius -> celsius + 273.15 },
        "kelvin",
        "K",
    )

// Extension properties for Double
inline val Double.celsius get() = Celsius.of(this)
inline val Double.fahrenheit get() = Fahrenheit.of(this)
inline val Double.kelvin get() = Kelvin.of(this)
