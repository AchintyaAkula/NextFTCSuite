/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("ForceUnits")

package dev.nextftc.units.unittypes

import dev.nextftc.units.Measure
import dev.nextftc.units.Unit
import dev.nextftc.units.measuretypes.Force

/**
 * Unit of measurement for force.
 *
 * Supported units include newtons (base unit), kilonewtons, pounds-force, and kilograms-force.
 * Force is dimensionally equivalent to mass × acceleration = mass × distance / time².
 */
class ForceUnit(
    baseUnit: ForceUnit?,
    toBaseConverter: (Double) -> Double,
    fromBaseConverter: (Double) -> Double,
    unitName: String,
    unitSymbol: String,
) : Unit<ForceUnit>(
    baseUnit,
    toBaseConverter,
    fromBaseConverter,
    unitName,
    unitSymbol,
) {
    internal constructor(
        baseUnit: ForceUnit,
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

    override fun of(magnitude: Double): Force = Force(magnitude, this)

    override fun ofBaseUnits(baseUnitMagnitude: Double): Measure<ForceUnit> =
        of(this.fromBaseUnits(baseUnitMagnitude))

    override fun per(time: TimeUnit): Unit<*> {
        TODO("Force rate units not yet implemented")
    }
}

// Conversion constants
private const val KILONEWTONS_PER_NEWTON = 1e3
private const val POUNDS_FORCE_PER_NEWTON = 4.4482216152605 // 1 lbf ≈ 4.448 N
private const val KILOGRAMS_FORCE_PER_NEWTON = 9.80665 // 1 kgf = 9.80665 N

// Force units
val Newtons = ForceUnit(null, { it }, { it }, "newton", "N")
val Kilonewtons = ForceUnit(Newtons, KILONEWTONS_PER_NEWTON, "kilonewton", "kN")
val PoundsForce = ForceUnit(Newtons, POUNDS_FORCE_PER_NEWTON, "pound-force", "lbf")
val KilogramsForce = ForceUnit(Newtons, KILOGRAMS_FORCE_PER_NEWTON, "kilogram-force", "kgf")

// Extension properties for Double
inline val Double.newtons get() = Newtons.of(this)
inline val Double.kilonewtons get() = Kilonewtons.of(this)
inline val Double.poundsForce get() = PoundsForce.of(this)
inline val Double.kilogramsForce get() = KilogramsForce.of(this)
