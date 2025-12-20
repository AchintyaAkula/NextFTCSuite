/*
 * Copyright (c) 2025 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.control2.feedback

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class SquIDControllerTest :
    FunSpec({
        context("SquIDController construction") {
            test("can be constructed with coefficients object") {
                val coefficients = PIDCoefficients(1.0, 2.0, 3.0)
                val controller = SquIDController(coefficients)
                controller.coefficients shouldBe coefficients
            }

            test("can be constructed with individual values") {
                val controller = SquIDController(1.0, 2.0, 3.0)
                controller.coefficients.kP shouldBe 1.0
                controller.coefficients.kI shouldBe 2.0
                controller.coefficients.kD shouldBe 3.0
            }

            test("default values work") {
                val controller = SquIDController(1.0)
                controller.coefficients.kP shouldBe 1.0
                controller.coefficients.kI shouldBe 0.0
                controller.coefficients.kD shouldBe 0.0
                controller.resetIntegralOnZeroCrossover shouldBe true
            }

            test("resetIntegralOnZeroCrossover can be disabled") {
                val controller = SquIDController(1.0, resetIntegralOnZeroCrossover = false)
                controller.resetIntegralOnZeroCrossover shouldBe false
            }
        }

        context("SquIDController P term (square root)") {
            test("proportional output is kP * sqrt(|error|) * sign(error) for positive error") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(2.0, 0.0, 0.0)

                // P = 2.0 * sqrt(4.0) * 1 = 2.0 * 2.0 = 4.0
                val output = controller.calculate(
                    timeSource.markNow(),
                    4.0,
                    0.0,
                )
                output shouldBe (4.0 plusOrMinus 0.001)
            }

            test("proportional output for negative error") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(2.0, 0.0, 0.0)

                // P = 2.0 * sqrt(|-4.0|) * (-1) = 2.0 * 2.0 * (-1) = -4.0
                val output = controller.calculate(timeSource.markNow(), -4.0, 0.0)
                output shouldBe (-4.0 plusOrMinus 0.001)
            }

            test("sqrt behavior differs from linear PID") {
                val timeSource = TestTimeSource()
                val squidController = SquIDController(1.0, 0.0, 0.0)
                val pidController = PIDController(1.0, 0.0, 0.0)

                val error = 9.0
                val squidOutput = squidController.calculate(timeSource.markNow(), error, 0.0)
                val pidOutput = pidController.calculate(timeSource.markNow(), error, 0.0)

                // SquID: 1.0 * sqrt(9) * 1 = 3.0
                // PID: 1.0 * 9 = 9.0
                squidOutput shouldBe (3.0 plusOrMinus 0.001)
                pidOutput shouldBe (9.0 plusOrMinus 0.001)
            }

            test("zero error produces zero output") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(2.0, 0.0, 0.0)

                val output = controller.calculate(timeSource.markNow(), 0.0, 0.0)
                output shouldBe (0.0 plusOrMinus 0.001)
            }

            test("small error has proportionally larger response than linear PID") {
                val timeSource = TestTimeSource()
                val squidController = SquIDController(1.0, 0.0, 0.0)
                val pidController = PIDController(1.0, 0.0, 0.0)

                val smallError = 0.01
                val squidOutput = squidController.calculate(timeSource.markNow(), smallError, 0.0)
                val pidOutput = pidController.calculate(timeSource.markNow(), smallError, 0.0)

                // SquID: sqrt(0.01) = 0.1
                // PID: 0.01
                // SquID response is 10x larger for small errors
                squidOutput shouldBe (0.1 plusOrMinus 0.001)
                pidOutput shouldBe (0.01 plusOrMinus 0.001)
            }
        }

        context("SquIDController D term") {
            test("derivative output uses provided errorDerivative") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(0.0, 0.0, 3.0)

                val output = controller.calculate(timeSource.markNow(), 0.0, 2.0)
                output shouldBe (6.0 plusOrMinus 0.001)
            }

            test("derivative term is linear (not sqrt)") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(0.0, 0.0, 5.0)

                val output = controller.calculate(timeSource.markNow(), 0.0, -3.0)
                output shouldBe (-15.0 plusOrMinus 0.001)
            }
        }

        context("SquIDController combined terms") {
            test("P (sqrt) and D (linear) terms combine correctly") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(2.0, 0.0, 3.0)

                // P: 2 * sqrt(4) * 1 = 4, D: 3 * 2 = 6, Total: 10
                val output = controller.calculate(timeSource.markNow(), 4.0, 2.0)
                output shouldBe (10.0 plusOrMinus 0.001)
            }
        }

        context("SquIDController reset") {
            test("reset clears internal state") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(1.0, 1.0, 1.0)

                controller.calculate(timeSource.markNow(), 4.0, 1.0)
                timeSource += 10.milliseconds
                controller.reset()

                // After reset, should behave like fresh controller
                val output = controller.calculate(timeSource.markNow(), 4.0, 1.0)
                // First call after reset: P = sqrt(4) = 2, I = 0, D = 1
                output shouldBe (3.0 plusOrMinus 0.001)
            }
        }

        context("SquIDController reference/measured overloads") {
            test("calculate with reference and measured computes error correctly") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(1.0, 0.0, 0.0)

                // reference - measured = 16 - 7 = 9
                // P = 1 * sqrt(9) * 1 = 3
                // Pass 0.0 for measuredDerivative since we're only testing P term
                val output = controller.calculate(
                    timeSource.markNow(),
                    16.0,
                    7.0,
                    0.0,
                )
                output shouldBe (3.0 plusOrMinus 0.001)
            }

            test("calculate with reference and measured derivatives") {
                val timeSource = TestTimeSource()
                val controller = SquIDController(0.0, 0.0, 3.0)

                // errorDerivative = refDeriv - measDeriv = 5 - 2 = 3
                // D = 3 * 3 = 9
                val output = controller.calculate(
                    timeSource.markNow(),
                    0.0,
                    0.0,
                    5.0,
                    2.0,
                )
                output shouldBe (9.0 plusOrMinus 0.001)
            }
        }
    })
