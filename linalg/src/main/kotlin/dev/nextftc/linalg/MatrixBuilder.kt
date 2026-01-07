/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.linalg

class MatrixBuilder<R : Nat, C : Nat> internal constructor(val natRows: R, val natCols: C) {
    private val rows = mutableListOf<DoubleArray>()

    fun row(vararg elements: Double) = apply {
        require(elements.size == natCols.num)
        rows.add(elements)
    }

    fun build(): Matrix<R, C> {
        require(rows.size == natRows.num)
        return Matrix(rows.toTypedArray())
    }
}

fun <R : Nat, C : Nat> buildMatrix(
    natRows: R,
    natCols: C,
    initializer: MatrixBuilder<R, C>.() -> Unit,
): Matrix<R, C> {
    val builder = MatrixBuilder<R, C>(natRows, natCols)
    builder.initializer()
    return builder.build()
}

val mat = buildMatrix(N3, N2) {
    row(1.0, 2.0)
    row(2.0, 3.0)
    row(4.0, 5.0)
}

