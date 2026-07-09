package dev.nextftc.robot.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.nextftc.robot.NextRobot

/**
 * Base class for all NextFTC OpModes.
 *
 * Automatically injects the scanned [dev.nextftc.robot.NextRobot] instance and handles the lifecycle
 * execution, calling various [OpModeHook]s and managing the internal command scheduler.
 *
 * @param hooks Internal hooks used to manage robot mechanisms and the scheduler loop.
 */
abstract class NextOpMode internal constructor(private val hooks: MutableList<OpModeHook>) :
  LinearOpMode() {
  /**
   * Secondary constructor invoked by the [NextFTCOpModeScanner] during automatic registration.
   *
   * @param robot The automatically resolved [dev.nextftc.robot.NextRobot] instance.
   * @param hooks Additional custom hooks to execute during the OpMode lifecycle.
   */
  constructor(robot: NextRobot, vararg hooks: OpModeHook) : this(hooks.toMutableList()) {
    this.hooks += RobotHook(robot)
    this.hooks += SchedulerHook
    this.hooks += MotorHook
    this.hooks += TelemetryHook
  }

  final override fun runOpMode() {
    hooks.forEach(OpModeHook::beforeInit)
    onInit()
    hooks.forEach(OpModeHook::afterInit)
    while (opModeInInit()) {
      hooks.forEach(OpModeHook::beforeDisabled)
      disabledPeriodic()
      hooks.forEach(OpModeHook::afterDisabled)
    }
    waitForStart()
    hooks.forEach(OpModeHook::beforeStart)
    onStart()
    hooks.forEach(OpModeHook::afterStart)
    while (opModeIsActive()) {
      hooks.forEach(OpModeHook::beforePeriodic)
      periodic()
      hooks.forEach(OpModeHook::afterPeriodic)
    }
    hooks.forEach(OpModeHook::beforeEnd)
    onEnd()
    hooks.forEach(OpModeHook::afterEnd)
  }

  /** Called exactly once after the INIT button is pressed. */
  open fun onInit() {}

  /** Called repeatedly while the OpMode is in the INIT phase. */
  open fun disabledPeriodic() {}

  /** Called exactly once after the PLAY button is pressed. */
  open fun onStart() {}

  /** Called repeatedly while the OpMode is actively running. */
  open fun periodic() {}

  /** Called exactly once when the OpMode finishes execution. */
  open fun onEnd() {}
}
