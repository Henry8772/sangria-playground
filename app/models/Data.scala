package models

object Episode extends Enumeration {
  val NEWHOPE, EMPIRE, JEDI = Value
}

trait Character {
  def id: String
  def name: String
}

case class Human(
  id: String,
  name: String) extends Character

case class Droid(
  id: String,
  name: String) extends Character

class CharacterRepo {
  import models.CharacterRepo._

  def getHero(episode: Option[Episode.Value]) =
    episode flatMap (_ ⇒ getHuman("1000")) getOrElse droids.last

  def getHuman(id: String): Option[Human] = humans.find(c ⇒ c.id == id)

  def getDroid(id: String): Option[Droid] = droids.find(c ⇒ c.id == id)
}

object CharacterRepo {
  val humans = List(
    Human(
      id = "first",
      name = "hello")
  )

  val droids = List(
    Droid(
      id = "$hello World !",
      name = "$message")
  )
}
