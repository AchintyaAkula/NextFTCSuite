/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.control2.filters

/**
 * Exponential Moving Average (EMA) filter.
 *
 * Applies the recurrence: `y[n] = alpha * x[n] + (1 - alpha) * y[n-1]`.
 * The first output uses `0.0` as the previous value unless [previous] is preset.
 *
 * @param alpha smoothing factor in [0.0, 1.0]; higher values weight newer samples more.
 */
class EMAFilter(val alpha: Double) {
    /**
     * Last output sample, or `null` before the first call to [calculate].
     */
    var previous: Double? = null
        private set

    init {
        require(alpha in 0.0..1.0) { "EMA alpha must be in [0.0, 1.0] but was $alpha" }
    }

    /**
     * Updates the filter with a new sample and returns the filtered value.
     */
    fun calculate(newValue: Double): Double {
        previous = alpha * newValue + (1 - alpha) * (previous ?: 0.0)
        return previous!!
    }
}