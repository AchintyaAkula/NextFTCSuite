/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.targeting.SearchTarget
import dev.frozenmilk.sinister.targeting.WideSearch
import dev.frozenmilk.sinister.util.log.Logger
import dev.frozenmilk.util.graph.Graph
import dev.frozenmilk.util.graph.rule.AdjacencyRule
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubclassOf

/**
 * Scans the user's project for an implementation of [NextRobot] during startup.
 * Maintains a reference to the robot class and instantiation strategy so that
 * [dev.nextftc.robot.opmode.NextFTCOpModeScanner] can properly inject the robot instance into OpModes.
 */
internal object RobotScanner : Scanner {
  /** The singleton or freshly constructed instance of the user's robot. */
  lateinit var robot: NextRobot

  /** The class reference of the user's robot. */
  lateinit var robotClass: KClass<*>

  var foundRobot = false
  var foundMultiple = false

  override val loadAdjacencyRule: AdjacencyRule<Scanner, Graph<Scanner>> = Scanner.INDEPENDENT
  override val unloadAdjacencyRule: AdjacencyRule<Scanner, Graph<Scanner>> = Scanner.INDEPENDENT

  override val targets: SearchTarget = WideSearch()

  override fun scan(loader: ClassLoader, cls: Class<*>) {
    val kcls = cls.kotlin

    if (kcls.visibility == KVisibility.PUBLIC && kcls.isSubclassOf(NextRobot::class) && !kcls.isAbstract) {
      RobotLog.i("Found NextFTC robot class: $cls")

      val objectInstance = kcls.objectInstance

      if (objectInstance != null) {
        robot = objectInstance as NextRobot
        robotClass = kcls

        if (foundRobot) {
          foundMultiple = true
        }
        foundRobot = true
        return
      }

      val constructor = kcls.constructors.find { it.parameters.isEmpty() }
      if (constructor != null) {
        robot = constructor.call() as NextRobot
        robotClass = kcls

        if (foundRobot) {
          foundMultiple = true
        }
        foundRobot = true
      }

      Logger.w(
        "NextFTC",
        "Unable to instantiate NextFTC robot class: $cls. Ensure it is either a singleton object or has a public no-argument constructor.",
      )
    }
  }

  override fun afterScan(loader: ClassLoader) {
    if (foundMultiple) {
      Logger.e(
        "NextFTC",
        "Found multiple NextFTC robot classes. Please ensure that there is only one in your project.",
      )
    }

    if (!foundRobot) {
      Logger.e(
        "NextFTC",
        "Unable to find a NextFTC robot class. Please ensure that there is one in your project.",
      )
    } else {
      Logger.i("NextFTC", "Found NextFTC robot class: $robotClass")
    }
  }

  override fun unload(loader: ClassLoader, cls: Class<*>) {}
}
