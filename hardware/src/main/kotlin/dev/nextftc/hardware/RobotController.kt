/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.hardware

import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import org.firstinspires.ftc.robotcore.internal.system.AppUtil

object RobotController {
  @JvmStatic
  val hardwareMap get() = OpModeManagerImpl.getOpModeManagerOfActivity(
    AppUtil.getInstance().activity,
  ).hardwareMap
}
