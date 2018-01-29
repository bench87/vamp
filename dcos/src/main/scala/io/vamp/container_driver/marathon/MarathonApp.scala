package io.vamp.container_driver.marathon

import io.vamp.container_driver.Docker
import io.vamp.model.artifact.{ HealthCheck, Port }

case class MarathonApp(
  id:           String,
  container:    Option[Container],
  ipAddress:    Option[MarathonAppIpAddress],
  instances:    Int,
  cpus:         Double,
  mem:          Int,
  env:          Map[String, String],
  cmd:          Option[String],
  healthChecks: List[MarathonHealthCheck]    = Nil,
  args:         List[String]                 = Nil,
  labels:       Map[String, String]          = Map(),
  constraints:  List[List[String]]           = Nil,
  fetch:        Option[List[UriObject]]
)

case class UriObject(uri: String, extract: Boolean = true)
case class Container(docker: Docker, `type`: String = "DOCKER")

case class MarathonAppIpAddress(networkName: String)

case class MarathonHealthCheck(
  path:                   String,
  port:                   Option[Int],
  portIndex:              Option[Int],
  protocol:               String,
  gracePeriodSeconds:     Int,
  intervalSeconds:        Int,
  timeoutSeconds:         Int,
  maxConsecutiveFailures: Int
)

object MarathonHealthCheck {

  /** Transforms a HealthCheck to a Marathon specific HealthCheck */
  def apply(ports: List[Port], healthCheck: HealthCheck): MarathonHealthCheck = {
    val index: Option[Int] = ports
      .zipWithIndex
      .find { case (p, i) ⇒ p.name.contains(healthCheck.port) || p.alias.contains(healthCheck.port) }
      .map(_._2)

    MarathonHealthCheck(
      healthCheck.path,
      None,
      index,
      healthCheck.protocol,
      healthCheck.initialDelay.value,
      healthCheck.interval.value,
      healthCheck.timeout.value,
      healthCheck.failures
    )
  }

  /**
   * Checks wether healthChecks are equal or not (ports needed for conversion to MarathonHealthCheck)
   */
  def equalHealthChecks(
    ports:                List[Port],
    healthChecks:         List[HealthCheck],
    marathonHealthChecks: List[MarathonHealthCheck]
  ): Boolean =
    if (healthChecks.isEmpty && marathonHealthChecks.isEmpty) true
    else if (healthChecks.length != marathonHealthChecks.length) false
    else healthChecks
      .map(MarathonHealthCheck.apply(ports, _))
      .forall(marathonHealthChecks.contains(_))

}
