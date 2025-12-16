/*
 * Copyright (c)  NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.units

import dev.nextftc.units.unittypes.meters
import dev.nextftc.units.unittypes.seconds
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunsiesTest : FunSpec({
    test("funsies") {
        val d = 10.0.meters
        val t = 2.0.seconds

        val v = d / t

        v.magnitude shouldBe 5.0
    }
})