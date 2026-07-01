/*
 * NextFTC: a user-friendly control library for FIRST Tech Challenge
 *     Copyright (C) 2025 Rowan McAlpin
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nextftc.hardware

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.math.PI

class AnalogFeedbackTest :
  FunSpec({
    class Holder(voltage: Double) {
      val angle by AnalogFeedback { voltage }
    }

    test("zero voltage maps to zero angle") {
      Holder(0.0).angle shouldBe (0.0 plusOrMinus 1e-9)
    }

    test("full-scale voltage (3.3V) maps to a full turn") {
      Holder(3.3).angle shouldBe (2 * PI plusOrMinus 1e-9)
    }

    test("half-scale voltage maps to half a turn") {
      Holder(1.65).angle shouldBe (PI plusOrMinus 1e-9)
    }

    test("voltage is read fresh on every access") {
      var voltage = 0.0
      val holder = object {
        val angle by AnalogFeedback { voltage }
      }
      holder.angle shouldBe (0.0 plusOrMinus 1e-9)
      voltage = 3.3
      holder.angle shouldBe (2 * PI plusOrMinus 1e-9)
    }
  })
