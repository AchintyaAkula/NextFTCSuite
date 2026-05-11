/*
 * Copyright (c) 2026 NextFTC Team
 *
 *  Use of this source code is governed by an BSD-3-clause
 *  license that can be found in the LICENSE.md file at the root of this repository or at
 *  https://opensource.org/license/bsd-3-clause.
 */

package dev.nextftc.robot.internal

import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.targeting.SearchTarget
import dev.frozenmilk.sinister.targeting.TeamCodeSearch
import dev.frozenmilk.util.graph.Graph
import dev.frozenmilk.util.graph.rule.AdjacencyRule
import dev.nextftc.robot.RobotBase
import java.util.Optional

object RobotScanner : Scanner {
    internal var robot: Optional<RobotBase> = Optional.empty()
        private set

    override val loadAdjacencyRule: AdjacencyRule<Scanner, Graph<Scanner>> = Scanner.INDEPENDENT
    override val unloadAdjacencyRule: AdjacencyRule<Scanner, Graph<Scanner>> = Scanner.INDEPENDENT
    override val targets: SearchTarget = TeamCodeSearch()

    override fun scan(loader: ClassLoader, cls: Class<*>) {
        if (robot.isPresent) return

        if (RobotBase::class.java.isAssignableFrom(cls)) {
            val robotInstance = cls.getDeclaredConstructor().newInstance() as RobotBase
            robot = Optional.of(robotInstance)
        }
    }

    override fun unload(loader: ClassLoader, cls: Class<*>) {
        robot = Optional.empty()
    }
}