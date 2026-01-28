/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.control2.util

import java.util.NavigableMap
import java.util.TreeMap

/**
 * A navigable map that supports flexible interpolation between values.
 *
 * `InterpolatingMap` is a wrapper around a `TreeMap<Double, T>` that, when queried with a key,
 * returns the exact value if the key exists, or uses a custom interpolation strategy otherwise.
 *
 * This class supports two interpolation approaches:
 * - **Local interpolation**: Using an `interpolate` function that takes two adjacent values and a
 *   parameter `t` in [0.0, 1.0]. Suitable for linear, polynomial, or other local interpolation methods.
 * - **Global interpolation**: Using a custom `getter` function that has access to the entire
 *   `NavigableMap`, allowing for spline interpolation or other methods that need multiple control points.
 *
 * Example usage with linear interpolation (local):
 * ```kotlin
 * val linearMap = InterpolatingMap<Double>(::lerp)
 * linearMap[0.0] = 0.0
 * linearMap[1.0] = 1.0
 * linearMap[2.0] = 4.0
 * val midpoint = linearMap[0.5]  // 0.5 (linear interpolation)
 * ```
 *
 * Example usage with cubic spline interpolation (global):
 * ```kotlin
 * val splineMap = InterpolatingMap<Double>({ key ->
 *     val keys = this.keys.toList()
 *     val values = this.values.toList()
 *     if (keys.size < 2) return@InterpolatingMap values.firstOrNull() ?: 0.0
 *     val spline = CubicSpline(keys, values)
 *     spline[key]
 * })
 * splineMap[0.0] = 0.0
 * splineMap[1.0] = 1.0
 * splineMap[2.0] = 0.5
 * val smooth = splineMap[1.5]  // Cubic spline interpolation
 * ```
 *
 * Note: For dedicated spline interpolation with better performance (caching), consider using
 * [CubicSpline] directly.
 *
 * @param T The type of values stored in the map.
 * @property tree The underlying `TreeMap` storing the data.
 * @property getter A function that takes a key and returns the interpolated value, with access to
 *   the entire `NavigableMap` for global interpolation strategies.
 *
 * @constructor Creates an empty `InterpolatingMap` with a custom getter function.
 * @constructor Creates an `InterpolatingMap` with a local interpolation function that takes two
 *   adjacent values and a parameter `t`.
 * @constructor Creates an `InterpolatingMap` with initial keys and values, using a custom getter.
 * @constructor Creates an `InterpolatingMap` with initial keys and values, using a local interpolator.
 *
 * @throws IllegalArgumentException if the number of keys and values do not match (constructor variants).
 */
class InterpolatingMap<T> private constructor(
    private val tree: TreeMap<Double, T>,
    private val getter: NavigableMap<Double, T>.(Double) -> T,
) : NavigableMap<Double, T> by tree {
    constructor(tree: TreeMap<Double, T>, interpolate: (T, T, Double) -> T) : this(tree, { key ->
        val low = floorEntry(key)
        val high = ceilingEntry(key)

        if (low.key == high.key) {
            tree[key]!!
        }

        val t = lerp(key, inputMin = low.key, inputMax = high.key, outputMin = 0.0, outputMax = 1.0)

        interpolate(low.value, high.value, t)
    })

    constructor(getter: NavigableMap<Double, T>.(Double) -> T) : this(TreeMap(), getter)

    constructor(interpolator: (T, T, Double) -> T) : this(TreeMap(), interpolator)

    constructor(getter: NavigableMap<Double, T>.(Double) -> T, keys: List<Double>, values: List<T>) : this(
        TreeMap(),
        getter,
    ) {
        require(keys.size == values.size) { "Keys and values must be the same size" }
        for (i in keys.indices) {
            tree[keys[i]] = values[i]
        }
    }

    constructor(interpolator: (T, T, Double) -> T, keys: List<Double>, values: List<T>) :
        this(TreeMap(), interpolator) {
        require(keys.size == values.size) { "Keys and values must be the same size" }
        for (i in keys.indices) {
            tree[keys[i]] = values[i]
        }
    }

    /**
     * Gets the value associated with the given key.
     * If the key does not exist,
     * the value is interpolated between the two nearest values.
     */
    override fun get(key: Double): T = this.getter(key)

    companion object {
        /**
         * Creates an empty linear interpolating map.
         *
         * Uses linear interpolation ([lerp]) to interpolate between two adjacent values.
         * Linear interpolation is simple and efficient, suitable for most general-purpose use cases.
         *
         * Example:
         * ```kotlin
         * val linearMap = InterpolatingMap.linear()
         * linearMap[0.0] = 0.0
         * linearMap[1.0] = 1.0
         * val value = linearMap[0.5]  // 0.5
         * ```
         *
         * @return an empty InterpolatingMap with linear interpolation
         */
        @JvmStatic fun linear() = InterpolatingMap(::lerp)

        /**
         * Creates a linear interpolating map initialized with the given keys and values.
         *
         * Uses linear interpolation ([lerp]) to interpolate between two adjacent values.
         *
         * @param keys the x-coordinates of the control points (will be sorted)
         * @param values the y-coordinates of the control points
         * @return an InterpolatingMap with the given data and linear interpolation
         * @throws IllegalArgumentException if keys.size != values.size
         */
        @JvmStatic fun linear(keys: List<Double>, values: List<Double>) = InterpolatingMap(::lerp, keys, values)

        /**
         * Creates an empty Catmull-Rom spline interpolating map.
         *
         * Uses Catmull-Rom spline interpolation ([splerp]) to provide smooth, continuous curves
         * that pass through all control points. This method uses neighboring points to compute
         * smooth tangents, resulting in visually pleasing interpolation.
         *
         * Suitable for motion profiles, smooth animations, and other applications requiring
         * smooth interpolation across multiple points.
         *
         * Example:
         * ```kotlin
         * val splineMap = InterpolatingMap.spline()
         * splineMap[0.0] = 0.0
         * splineMap[1.0] = 1.0
         * splineMap[2.0] = 0.5
         * val value = splineMap[1.5]  // Smooth spline interpolation
         * ```
         *
         * @return an empty InterpolatingMap with Catmull-Rom spline interpolation
         */
        @JvmStatic fun spline() = InterpolatingMap(::splerp)

        /**
         * Creates a Catmull-Rom spline interpolating map initialized with the given keys and values.
         *
         * Uses Catmull-Rom spline interpolation ([splerp]) to provide smooth, continuous curves
         * that pass through all control points.
         *
         * @param keys the x-coordinates of the control points (will be sorted)
         * @param values the y-coordinates of the control points
         * @return an InterpolatingMap with the given data and Catmull-Rom spline interpolation
         * @throws IllegalArgumentException if keys.size != values.size
         */
        @JvmStatic fun spline(keys: List<Double>, values: List<Double>) = InterpolatingMap(::splerp, keys, values)
    }
}
