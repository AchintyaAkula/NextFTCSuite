/*
 * Copyright (c)  NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.linalg

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class SizedMatrixTest :
    FunSpec({
        context("Construction") {
            test("zero matrix has all zeros") {
                val m = SizedMatrix.zero(N2, N3)
                m.numRows shouldBe 2
                m.numColumns shouldBe 3
                for (i in 0 until 2) {
                    for (j in 0 until 3) {
                        m[i, j] shouldBe 0.0
                    }
                }
            }

            test("identity creates identity matrix") {
                val m = SizedMatrix.identity(N3)
                m.numRows shouldBe 3
                m.numColumns shouldBe 3
                m[0, 0] shouldBe 1.0
                m[1, 1] shouldBe 1.0
                m[2, 2] shouldBe 1.0
                m[0, 1] shouldBe 0.0
                m[1, 0] shouldBe 0.0
            }

            test("diagonal creates diagonal matrix") {
                val m = SizedMatrix.diagonal(N3, 1.0, 2.0, 3.0)
                m.numRows shouldBe 3
                m.numColumns shouldBe 3
                m[0, 0] shouldBe 1.0
                m[1, 1] shouldBe 2.0
                m[2, 2] shouldBe 3.0
                m[0, 1] shouldBe 0.0
            }

            test("diagonal throws on size mismatch") {
                shouldThrow<IllegalArgumentException> {
                    SizedMatrix.diagonal(N3, 1.0, 2.0)
                }
            }

            test("row creates row vector") {
                val m = SizedMatrix.row(N3, 1.0, 2.0, 3.0)
                m.numRows shouldBe 1
                m.numColumns shouldBe 3
                m[0, 0] shouldBe 1.0
                m[0, 1] shouldBe 2.0
                m[0, 2] shouldBe 3.0
            }

            test("row throws on size mismatch") {
                shouldThrow<IllegalArgumentException> {
                    SizedMatrix.row(N3, 1.0, 2.0)
                }
            }

            test("column creates column vector") {
                val m = SizedMatrix.column(N3, 1.0, 2.0, 3.0)
                m.numRows shouldBe 3
                m.numColumns shouldBe 1
                m[0, 0] shouldBe 1.0
                m[1, 0] shouldBe 2.0
                m[2, 0] shouldBe 3.0
            }

            test("column throws on size mismatch") {
                shouldThrow<IllegalArgumentException> {
                    SizedMatrix.column(N3, 1.0, 2.0)
                }
            }

            test("from creates matrix from 2D array") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(
                        doubleArrayOf(1.0, 2.0),
                        doubleArrayOf(3.0, 4.0),
                    ),
                )
                m.numRows shouldBe 2
                m.numColumns shouldBe 2
                m[0, 0] shouldBe 1.0
                m[0, 1] shouldBe 2.0
                m[1, 0] shouldBe 3.0
                m[1, 1] shouldBe 4.0
            }

            test("from throws on row count mismatch") {
                shouldThrow<IllegalArgumentException> {
                    SizedMatrix.from(
                        N3,
                        N2,
                        arrayOf(
                            doubleArrayOf(1.0, 2.0),
                            doubleArrayOf(3.0, 4.0),
                        ),
                    )
                }
            }

            test("from throws on column count mismatch") {
                shouldThrow<IllegalArgumentException> {
                    SizedMatrix.from(
                        N2,
                        N3,
                        arrayOf(
                            doubleArrayOf(1.0, 2.0),
                            doubleArrayOf(3.0, 4.0),
                        ),
                    )
                }
            }

            test("2D array constructor") {
                val m = SizedMatrix<N2, N2>(
                    arrayOf(
                        doubleArrayOf(1.0, 2.0),
                        doubleArrayOf(3.0, 4.0),
                    ),
                )
                m.numRows shouldBe 2
                m.numColumns shouldBe 2
                m[0, 0] shouldBe 1.0
                m[1, 1] shouldBe 4.0
            }
        }

        context("Properties") {
            test("size returns correct dimensions") {
                val m = SizedMatrix.zero(N2, N3)
                m.size shouldBe (2 to 3)
            }

            test("transpose swaps dimensions and types") {
                val m = SizedMatrix.from(
                    N2,
                    N3,
                    arrayOf(
                        doubleArrayOf(1.0, 2.0, 3.0),
                        doubleArrayOf(4.0, 5.0, 6.0),
                    ),
                )
                val t: SizedMatrix<N3, N2> = m.transpose
                t.numRows shouldBe 3
                t.numColumns shouldBe 2
                t[0, 0] shouldBe 1.0
                t[0, 1] shouldBe 4.0
                t[1, 0] shouldBe 2.0
                t[2, 0] shouldBe 3.0
            }

            test("inverse computes correctly") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(
                        doubleArrayOf(4.0, 7.0),
                        doubleArrayOf(2.0, 6.0),
                    ),
                )
                val inv: SizedMatrix<N2, N2> = m.inverse
                val product = m * inv
                product[0, 0] shouldBe (1.0 plusOrMinus 1e-9)
                product[1, 1] shouldBe (1.0 plusOrMinus 1e-9)
                product[0, 1] shouldBe (0.0 plusOrMinus 1e-9)
                product[1, 0] shouldBe (0.0 plusOrMinus 1e-9)
            }

            test("pseudoInverse has correct dimensions") {
                val m = SizedMatrix.from(
                    N3,
                    N2,
                    arrayOf(
                        doubleArrayOf(1.0, 2.0),
                        doubleArrayOf(3.0, 4.0),
                        doubleArrayOf(5.0, 6.0),
                    ),
                )
                val pinv: SizedMatrix<N2, N3> = m.pseudoInverse
                pinv.numRows shouldBe 2
                pinv.numColumns shouldBe 3
            }

            test("norm computes Frobenius norm") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(
                        doubleArrayOf(1.0, 2.0),
                        doubleArrayOf(3.0, 4.0),
                    ),
                )
                // Frobenius norm = sqrt(1 + 4 + 9 + 16) = sqrt(30)
                m.norm shouldBe (kotlin.math.sqrt(30.0) plusOrMinus 1e-9)
            }
        }

        context("Element access") {
            test("get returns correct element") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(
                        doubleArrayOf(1.0, 2.0),
                        doubleArrayOf(3.0, 4.0),
                    ),
                )
                m[0, 0] shouldBe 1.0
                m[0, 1] shouldBe 2.0
                m[1, 0] shouldBe 3.0
                m[1, 1] shouldBe 4.0
            }

            test("set modifies element") {
                val m = SizedMatrix.zero(N2, N2)
                m[0, 1] = 5.0
                m[0, 1] shouldBe 5.0
            }
        }

        context("Arithmetic operations") {
            test("unary minus negates all elements") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(
                        doubleArrayOf(1.0, -2.0),
                        doubleArrayOf(-3.0, 4.0),
                    ),
                )
                val neg: SizedMatrix<N2, N2> = -m
                neg[0, 0] shouldBe -1.0
                neg[0, 1] shouldBe 2.0
                neg[1, 0] shouldBe 3.0
                neg[1, 1] shouldBe -4.0
            }

            test("plus adds matrices element-wise") {
                val a = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val b = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(5.0, 6.0), doubleArrayOf(7.0, 8.0)),
                )
                val c: SizedMatrix<N2, N2> = a + b
                c[0, 0] shouldBe 6.0
                c[0, 1] shouldBe 8.0
                c[1, 0] shouldBe 10.0
                c[1, 1] shouldBe 12.0
            }

            test("minus subtracts matrices element-wise") {
                val a = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(5.0, 6.0), doubleArrayOf(7.0, 8.0)),
                )
                val b = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val c: SizedMatrix<N2, N2> = a - b
                c[0, 0] shouldBe 4.0
                c[0, 1] shouldBe 4.0
                c[1, 0] shouldBe 4.0
                c[1, 1] shouldBe 4.0
            }

            test("times multiplies matrices with type-safe dimensions") {
                val a: SizedMatrix<N2, N3> = SizedMatrix.from(
                    N2,
                    N3,
                    arrayOf(
                        doubleArrayOf(1.0, 2.0, 3.0),
                        doubleArrayOf(4.0, 5.0, 6.0),
                    ),
                )
                val b: SizedMatrix<N3, N2> = SizedMatrix.from(
                    N3,
                    N2,
                    arrayOf(
                        doubleArrayOf(7.0, 8.0),
                        doubleArrayOf(9.0, 10.0),
                        doubleArrayOf(11.0, 12.0),
                    ),
                )
                val c: SizedMatrix<N2, N2> = a * b
                c.numRows shouldBe 2
                c.numColumns shouldBe 2
                c[0, 0] shouldBe (1.0 * 7.0 + 2.0 * 9.0 + 3.0 * 11.0)
                c[0, 1] shouldBe (1.0 * 8.0 + 2.0 * 10.0 + 3.0 * 12.0)
            }

            test("times scalar multiplies all elements") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val result: SizedMatrix<N2, N2> = m * 2.0
                result[0, 0] shouldBe 2.0
                result[0, 1] shouldBe 4.0
                result[1, 0] shouldBe 6.0
                result[1, 1] shouldBe 8.0
            }

            test("times int scalar multiplies all elements") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val result: SizedMatrix<N2, N2> = m * 2
                result[0, 0] shouldBe 2.0
                result[1, 1] shouldBe 8.0
            }

            test("scalar times matrix from left") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val result: SizedMatrix<N2, N2> = 2.0 * m
                result[0, 0] shouldBe 2.0
                result[1, 1] shouldBe 8.0
            }

            test("int scalar times matrix from left") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val result: SizedMatrix<N2, N2> = 2 * m
                result[0, 0] shouldBe 2.0
                result[1, 1] shouldBe 8.0
            }
        }

        context("Solve") {
            test("solve computes solution with type-safe dimensions") {
                val a: SizedMatrix<N2, N2> = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(
                        doubleArrayOf(2.0, 1.0),
                        doubleArrayOf(1.0, 3.0),
                    ),
                )
                val b: SizedMatrix<N2, N1> = SizedMatrix.column(N2, 5.0, 10.0)
                val x: SizedMatrix<N2, N1> = a.solve(b)
                // Verify: A * x ≈ b
                val result = a * x
                result[0, 0] shouldBe (5.0 plusOrMinus 1e-9)
                result[1, 0] shouldBe (10.0 plusOrMinus 1e-9)
            }
        }

        context("Type safety") {
            test("matrix multiplication produces correct result type") {
                val m2x3: SizedMatrix<N2, N3> = SizedMatrix.zero(N2, N3)
                val m3x4: SizedMatrix<N3, N4> = SizedMatrix.zero(N3, N4)
                val m2x4: SizedMatrix<N2, N4> = m2x3 * m3x4
                m2x4.numRows shouldBe 2
                m2x4.numColumns shouldBe 4
            }

            test("transpose produces correct result type") {
                val m2x3: SizedMatrix<N2, N3> = SizedMatrix.zero(N2, N3)
                val m3x2: SizedMatrix<N3, N2> = m2x3.transpose
                m3x2.numRows shouldBe 3
                m3x2.numColumns shouldBe 2
            }
        }

        context("Conversion") {
            test("toDynamicMatrix converts to dynamic matrix") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val dm = m.toDynamicMatrix()
                dm.numRows shouldBe 2
                dm.numColumns shouldBe 2
                dm[0, 0] shouldBe 1.0
                dm[1, 1] shouldBe 4.0
            }
        }

        context("Utility") {
            test("copy creates independent copy") {
                val m = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val copy = m.copy()
                copy[0, 0] = 10.0
                m[0, 0] shouldBe 1.0
                copy[0, 0] shouldBe 10.0
            }

            test("equals compares matrices") {
                val a = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val b = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)),
                )
                val c = SizedMatrix.from(
                    N2,
                    N2,
                    arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 5.0)),
                )
                (a == b) shouldBe true
                (a == c) shouldBe false
            }

            test("toString formats correctly") {
                val m = SizedMatrix.identity(N2)
                m.toString().contains("SizedMatrix<2, 2>") shouldBe true
            }
        }
    })
