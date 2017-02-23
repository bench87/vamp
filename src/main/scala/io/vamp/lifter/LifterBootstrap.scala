package io.vamp.lifter

import akka.actor.{ ActorRef, ActorSystem }
import io.vamp.common.Config
import io.vamp.common.akka.{ ActorBootstrap, IoC }
import io.vamp.lifter.artifact.ArtifactInitializationActor
import io.vamp.lifter.persistence.ElasticsearchPersistenceInitializationActor
import io.vamp.lifter.pulse.ElasticsearchPulseInitializationActor
import io.vamp.persistence.PersistenceBootstrap
import io.vamp.pulse.PulseBootstrap

class LifterBootstrap extends ActorBootstrap {

  private val pulseEnabled = Config.boolean("vamp.lifter.pulse.enabled")()

  private val artifactEnabled = Config.boolean("vamp.lifter.artifact.enabled")()

  def createActors(implicit actorSystem: ActorSystem): List[ActorRef] = {

    val persistence = if (Config.boolean("vamp.lifter.persistence.enabled")()) {
      PersistenceBootstrap.databaseType().toLowerCase match {
        case "elasticsearch" ⇒ IoC.createActor[ElasticsearchPersistenceInitializationActor] :: Nil
        case _               ⇒ Nil
      }
    } else Nil

    val pulse = if (pulseEnabled) {
      PulseBootstrap.`type`().toLowerCase match {
        case "elasticsearch" ⇒ IoC.createActor[ElasticsearchPulseInitializationActor] :: Nil
        case _               ⇒ Nil
      }
    } else Nil

    val artifact = if (artifactEnabled)
      IoC.createActor[ArtifactInitializationActor] :: Nil
    else Nil

    persistence ++ pulse ++ artifact
  }

  override def restart(implicit actorSystem: ActorSystem): Unit = {}
}
