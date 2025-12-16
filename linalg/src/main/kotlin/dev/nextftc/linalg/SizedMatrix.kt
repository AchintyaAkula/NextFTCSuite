/*
 * Copyright (c)  NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

@file:JvmName("SizedMatrices")
@file:Suppress("UNUSED_PARAMETER")

package dev.nextftc.linalg

import org.ejml.simple.SimpleMatrix

/**
 * A dimensionally type-safe matrix of doubles.
 *
 * The dimensions [R] (rows) and [C] (columns) are encoded at the type level using [Nat] types,
 * allowing the compiler to catch dimension mismatches at compile time.
 *
 * Example:
 * ```kotlin
 * val a: SizedMatrix<N2, N3> = SizedMatrix.zero(N2, N3)  // 2x3 matrix
 * val b: SizedMatrix<N3, N4> = SizedMatrix.zero(N3, N4)  // 3x4 matrix
 * val c: SizedMatrix<N2, N4> = a * b                      // 2x4 matrix - compiles!
 * // val d = a * a  // Would not compile - N3 != N2
 * ```
 *
 * @param R The row dimension type
 * @param C The column dimension type
 */
open class SizedMatrix<R : Nat, C : Nat> internal constructor(
    internal val simple: SimpleMatrix,
    internal val rowNat: R,
    internal val colNat: C,
) {
    @Suppress("UNCHECKED_CAST")
    constructor(data: Array<DoubleArray>) :
            this(SimpleMatrix(data), natOf(data.size) as R, natOf(data.first().size) as C)

    companion object {
        /**
         * Creates a zero matrix with dimensions specified by the [Nat] type parameters.
         */
        @JvmStatic
        fun <R : Nat, C : Nat> zero(rows: R, cols: C): SizedMatrix<R, C> =
            SizedMatrix(SimpleMatrix(rows.num, cols.num), rows, cols)

        /**
         * Creates a zero matrix with dimensions [rows] x [cols].
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <R: Nat, C: Nat> zero(rows: Int, cols: Int) : SizedMatrix<R, C> {
            val rNat = natOf(rows)
            val cNat = natOf(cols)
            return zero(rNat, cNat) as SizedMatrix<R, C>
        }

        /**
         * Creates an identity matrix with dimensions [size] x [size].
         */
        @JvmStatic
        fun <N : Nat> identity(size: N): SizedMatrix<N, N> =
            SizedMatrix(SimpleMatrix.identity(size.num), size, size)

        /**
         * Creates a matrix with [data] along the diagonal.
         */
        @JvmStatic
        fun <N : Nat> diagonal(size: N, vararg data: Double): SizedMatrix<N, N> {
            require(data.size == size.num) { "Data size must match dimension" }
            return SizedMatrix(SimpleMatrix.diag(*data), size, size)
        }

        /**
         * Creates a row vector (1 x C matrix).
         */
        @JvmStatic
        fun <C : Nat> row(cols: C, vararg data: Double): SizedMatrix<N1, C> {
            require(data.size == cols.num) { "Data size must match column dimension" }
            return SizedMatrix(SimpleMatrix(1, data.size, true, data), N1, cols)
        }

        /**
         * Creates a column vector (R x 1 matrix).
         */
        @JvmStatic
        fun <R : Nat> column(rows: R, vararg data: Double): SizedMatrix<R, N1> {
            require(data.size == rows.num) { "Data size must match row dimension" }
            return SizedMatrix(SimpleMatrix(data.size, 1, false, data), rows, N1)
        }

        /**
         * Creates a matrix from a 2D array with specified dimensions.
         */
        @JvmStatic
        fun <R : Nat, C : Nat> from(
            rows: R,
            cols: C,
            data: Array<DoubleArray>,
        ): SizedMatrix<R, C> {
            require(data.size == rows.num) { "Row count must match row dimension" }
            require(data.all { it.size == cols.num }) { "All rows must have column dimension size" }
            return SizedMatrix(SimpleMatrix(data), rows, cols)
        }
    }

    /** The number of rows in the matrix. */
    @JvmField
    val numRows: Int = simple.numRows()

    /** The number of columns in the matrix. */
    @JvmField
    val numColumns: Int = simple.numCols()

    /** The size of the matrix as (rows, columns). */
    @JvmField
    val size: Pair<Int, Int> = numRows to numColumns

    /** The transpose of this matrix, with swapped dimension types. */
    @get:JvmName("transpose")
    val transpose: SizedMatrix<C, R>
        get() = SizedMatrix(simple.transpose(), colNat, rowNat)

    /** Returns a copy of this matrix. */
    open fun copy(): SizedMatrix<R, C> = SizedMatrix(simple.copy(), rowNat, colNat)

    /** The inverse of this matrix. Only valid for square matrices. */
    @get:JvmName("inverse")
    val inverse: SizedMatrix<R, C>
        get() = SizedMatrix(simple.invert(), rowNat, colNat)

    /** The pseudo-inverse of this matrix. */
    @get:JvmName("pseudoInverse")
    val pseudoInverse: SizedMatrix<C, R>
        get() = SizedMatrix(simple.pseudoInverse(), colNat, rowNat)

    /** The Frobenius norm of this matrix. */
    @get:JvmName("norm")
    val norm: Double
        get() = simple.normF()

    /** Negates all elements of this matrix. */
    open operator fun unaryMinus(): SizedMatrix<R, C> =
        SizedMatrix(simple.negative(), rowNat, colNat)

    /** Adds another matrix with the same dimensions. */
    operator fun plus(other: SizedMatrix<R, C>): SizedMatrix<R, C> =
        SizedMatrix(simple + other.simple, rowNat, colNat)

    /** Subtracts another matrix with the same dimensions. */
    operator fun minus(other: SizedMatrix<R, C>): SizedMatrix<R, C> =
        SizedMatrix(simple - other.simple, rowNat, colNat)

    /**
     * Multiplies this matrix by another matrix.
     * The inner dimensions must match: (R x C) * (C x K) = (R x K)
     */
    operator fun <K : Nat> times(other: SizedMatrix<C, K>): SizedMatrix<R, K> =
        SizedMatrix(simple.mult(other.simple), rowNat, other.colNat)

    /** Multiplies this matrix by a scalar. */
    open operator fun times(scalar: Double): SizedMatrix<R, C> =
        SizedMatrix(simple.scale(scalar), rowNat, colNat)

    /** Multiplies this matrix by a scalar. */
    open operator fun times(scalar: Int): SizedMatrix<R, C> = times(scalar.toDouble())

    /**
     * Solves for X in the equation AX = B,
     * where A is this matrix and B is [other].
     */
    fun <K : Nat> solve(other: SizedMatrix<R, K>): SizedMatrix<C, K> =
        SizedMatrix(simple.solve(other.simple), colNat, other.colNat)

    /** Returns the element at the given indices. */
    operator fun get(i: Int, j: Int): Double = simple[i, j]

    /** Sets the element at the given indices. */
    operator fun set(i: Int, j: Int, value: Double) {
        simple[i, j] = value
    }

    /** Converts to a [DynamicMatrix]. */
    fun toDynamicMatrix(): DynamicMatrix = DynamicMatrix(simple)

    override fun toString(): String = buildString {
        append("SizedMatrix<$numRows, $numColumns>:\n")
        for (i in 0 until numRows) {
            append("[ ")
            for (j in 0 until numColumns) {
                append("%10.4f".format(simple[i, j]))
                if (j < numColumns - 1) append(", ")
            }
            append(" ]\n")
        }
    }.trimEnd()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is SizedMatrix<*, *> && this.simple.isIdentical(other.simple, 1e-6)
    }

    override fun hashCode(): Int = simple.hashCode()
}

/** Scalar multiplication from the left. */
operator fun <R : Nat, C : Nat> Double.times(matrix: SizedMatrix<R, C>): SizedMatrix<R, C> =
    matrix * this

/** Scalar multiplication from the left. */
operator fun <R : Nat, C : Nat> Int.times(matrix: SizedMatrix<R, C>): SizedMatrix<R, C> =
    matrix * this
