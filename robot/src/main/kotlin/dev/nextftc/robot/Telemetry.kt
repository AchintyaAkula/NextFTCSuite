package dev.nextftc.robot

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import dev.frozenmilk.sinister.sdk.apphooks.OnCreateEventLoop
import org.firstinspires.ftc.robotcore.external.Telemetry as SdkTelemetry

/**
 * Unified telemetry routing system.
 *
 * Provides a global entry point to broadcast telemetry data across multiple [TelemetryBackend]s
 * simultaneously (e.g., standard Driver Station, FTC Dashboard, custom loggers).
 */
object Telemetry {
  private val backends: MutableSet<TelemetryBackend> = mutableSetOf(DriverStationTelemetry)

  /**
   * Registers a new [TelemetryBackend] to receive telemetry updates.
   *
   * @param backend The backend implementation to add.
   */
  @JvmStatic fun addBackend(backend: TelemetryBackend) {
    backends += backend
  }

  /**
   * Helper function to wrap and register a standard FTC SDK [SdkTelemetry] instance
   * as a backend.
   *
   * @param backend The FTC SDK Telemetry instance.
   */
  @JvmStatic fun addBackend(backend: SdkTelemetry) {
    addBackend(SdkTelemetryBackend(backend))
  }

  /**
   * Triggers an update/flush on all registered backends.
   * Called automatically by [TelemetryHook].
   */
  @JvmStatic fun update() {
    backends.forEach(TelemetryBackend::update)
  }

  /**
   * Logs a key-value pair to all registered backends.
   *
   * @param key The caption/label for the data.
   * @param value The string value to display.
   */
  @JvmStatic fun log(key: String, value: String) {
    backends.forEach { it.addData(key, value) }
  }

  /**
   * Logs a key-value pair to all registered backends, automatically calling `toString()`.
   *
   * @param key The caption/label for the data.
   * @param value The object to display.
   */
  @JvmStatic fun log(key: String, value: Any?) {
    log(key, value?.toString() ?: "null")
  }

  /**
   * Logs a raw string line to all registered backends.
   *
   * @param line The string line to display.
   */
  @JvmStatic fun log(line: String) {
    backends.forEach { it.addLine(line) }
  }
}

/**
 * Defines a destination that can receive telemetry data.
 * Implement this interface to create custom telemetry outputs (e.g., FtcDashboard, file logger).
 */
interface TelemetryBackend {
  /**
   * Adds a key-value pair to the telemetry buffer.
   */
  fun addData(key: String, value: String)

  /**
   * Adds a raw line to the telemetry buffer.
   */
  fun addLine(line: String) = addData(line, "")

  /**
   * Flushes the buffer to the actual display or log.
   */
  fun update()
}

/**
 * A wrapper to convert a standard FTC SDK Telemetry instance into a [TelemetryBackend].
 */
internal class SdkTelemetryBackend(val sdkTelemetry: SdkTelemetry) : TelemetryBackend {
  override fun update() {
    sdkTelemetry.update()
  }

  override fun addData(key: String, value: String) {
    sdkTelemetry.addData(key, value)
  }

  override fun addLine(line: String) {
    sdkTelemetry.addLine(line)
  }

  override fun equals(other: Any?): Boolean =
    other is SdkTelemetryBackend && sdkTelemetry == other.sdkTelemetry

  override fun hashCode(): Int = sdkTelemetry.hashCode()
}

/**
 * A backend for sending telemetry data to the Driver Station.
 */
internal object DriverStationTelemetry : TelemetryBackend {
  lateinit var telemetry: SdkTelemetry

  override fun update() {
    if (::telemetry.isInitialized) {
      telemetry.update()
    }
  }

  override fun addData(key: String, value: String) {
    if (::telemetry.isInitialized) {
      telemetry.addData(key, value)
    }
  }

  override fun addLine(line: String) {
    if (::telemetry.isInitialized) {
      telemetry.addLine(line)
    }
  }
}
