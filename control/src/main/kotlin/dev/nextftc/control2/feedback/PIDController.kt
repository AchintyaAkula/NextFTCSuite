/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.control2.feedback

import kotlin.math.sign
import kotlin.time.ComparableTimeMark
import kotlin.time.DurationUnit

/**
 * Coefficients for a PID Controller/
 *
 * @param kP proportional gain, multiplied by the error
 * @param kI integral gain, multiplied by the integral of the error over time
 * @param kD derivative gain, multiplied by the derivative of the error
 */
data class PIDCoefficients @JvmOverloads constructor(
    @JvmField var kP: Double,
    @JvmField var kI: Double = 0.0,
    @JvmField var kD: Double = 0.0,
)

/**
 * Traditional proportional-integral-derivative controller.
 *
 * @param coefficients the [PIDCoefficients] that contains the PID gains
 * @param resetIntegralOnZeroCrossover whether to reset the integral term when the error crosses
 */
class PIDController @JvmOverloads constructor(
    val coefficients: PIDCoefficients,
    val resetIntegralOnZeroCrossover: Boolean = true,
) {
    private var lastError: Double = 0.0
    private var errorSum = 0.0
    private var lastTimestamp: ComparableTimeMark? = null

    /**
     * Creates a PIDController with the given coefficients.
     *
     * @param kP proportional gain, multiplied by the error
     * @param kI integral gain, multiplied by the integral of the error over time
     * @param kD derivative gain, multiplied by the derivative of the error
     */
    @JvmOverloads constructor(
        kP: Double,
        kI: Double = 0.0,
        kD: Double = 0.0,
        resetIntegralOnZeroCrossover: Boolean = true,
    ) : this(
        PIDCoefficients(kP, kI, kD),
        resetIntegralOnZeroCrossover,
    )

    /**
     * Calculates the PID output
     *
     * @param timestamp the current time
     * @param error the error in the target state; the difference between the desired
     *  state and the current state
     * @param errorDerivative the derivative of the error, or `null` to compute it automatically
     *  from the change in error over time. This is typically the difference between the desired
     *  and current velocity when controlling position, or the difference in acceleration when
     *  controlling velocity.
     *
     * @return the PID output
     */
    fun calculate(timestamp: ComparableTimeMark, error: Double, errorDerivative: Double?): Double {
        if (lastTimestamp == null) {
            lastError = error
            lastTimestamp = timestamp
            // On first call with no derivative provided, ignore D term
            val derivative = errorDerivative ?: 0.0
            return coefficients.kP * error + coefficients.kD * derivative
        }

        if (resetIntegralOnZeroCrossover && lastError.sign != error.sign) {
            errorSum = 0.0
        }

        val deltaT = (timestamp - lastTimestamp!!).toDouble(DurationUnit.NANOSECONDS)
        errorSum += error * deltaT

        val derivative = errorDerivative ?: ((error - lastError) / deltaT)

        lastError = error
        lastTimestamp = timestamp

        return coefficients.kP * error + coefficients.kI * errorSum + coefficients.kD *
            derivative
    }

    /**
     * Calculates the PID output from a reference (setpoint) and measured value.
     *
     * This overload assumes the reference derivative is zero (i.e., the setpoint is constant).
     *
     * @param timestamp the current time
     * @param reference the desired/target value (setpoint)
     * @param measured the current measured value
     * @param measuredDerivative the derivative of the measured value, or `null` to compute the
     *  error derivative automatically from the change in error over time.
     *
     * @return the PID output
     */
    fun calculate(
        timestamp: ComparableTimeMark,
        reference: Double,
        measured: Double,
        measuredDerivative: Double?,
    ): Double = calculate(
        timestamp,
        reference - measured,
        measuredDerivative?.let { -it },
    )

    /**
     * Calculates the PID output from a reference (setpoint) and measured value, with their
     * respective derivatives.
     *
     * @param timestamp the current time
     * @param reference the desired/target value (setpoint)
     * @param measured the current measured value
     * @param referenceDerivative the derivative of the reference value (e.g., desired velocity)
     * @param measuredDerivative the derivative of the measured value (e.g., current velocity)
     *
     * @return the PID output
     */
    fun calculate(
        timestamp: ComparableTimeMark,
        reference: Double,
        measured: Double,
        referenceDerivative: Double,
        measuredDerivative: Double,
    ): Double = calculate(
        timestamp,
        reference - measured,
        referenceDerivative - measuredDerivative,
    )

    /**
     * Resets the PID controller
     */
    fun reset() {
        errorSum = 0.0
        lastError = 0.0
        lastTimestamp = null
    }
}
