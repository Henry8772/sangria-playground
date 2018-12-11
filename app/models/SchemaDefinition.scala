package models

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {
  /**
    * Resolves the lists of characters. These resolutions are batched and
    * cached for the duration of a query.
    */
  val characters = Fetcher.caching(
    (ctx: CharacterRepo, ids: Seq[String]) ⇒
      Future.successful(ids.flatMap(id ⇒ ctx.getHuman(id) orElse ctx.getDroid(id))))(HasId(_.id))

  val EpisodeEnum = EnumType(
    "Episode",
    Some("One of the films in the Star Wars Trilogy"),
    List(
      EnumValue("NEWHOPE",
        value = Episode.NEWHOPE,
        description = Some("Released in 1977.")),
      EnumValue("EMPIRE",
        value = Episode.EMPIRE,
        description = Some("Released in 1980.")),
      EnumValue("JEDI",
        value = Episode.JEDI,
        description = Some("Released in 1983."))))

  val Character: InterfaceType[CharacterRepo, Character] =
    InterfaceType(
      "Character",
      "A character in the Star Wars Trilogy",
      () ⇒ fields[CharacterRepo, Character](
        Field("id", StringType,
          Some("The id of the character."),
          resolve = _.value.id),
        Field("name", StringType,
          Some("The name of the character."),
          resolve = _.value.name)
      ))

  val Human =
    ObjectType(
      "Human",
      "A humanoid creature in the Star Wars universe.",
      interfaces[CharacterRepo, Human](Character),
      fields[CharacterRepo, Human](
        Field("id", StringType,
          Some("The id of the human."),
          resolve = _.value.id),
        Field("name", StringType,
          Some("The name of the human."),
          resolve = _.value.name)
      ))

  val Droid = ObjectType(
    "Droid",
    "A mechanical creature in the Star Wars universe.",
    interfaces[CharacterRepo, Droid](Character),
    fields[CharacterRepo, Droid](
      Field("id", StringType,
        Some("The id of the droid."),
        tags = ProjectionName("_id") :: Nil,
        resolve = _.value.id),
      Field("name", StringType,
        Some("The name of the droid."),
        resolve = ctx ⇒ Future.successful(ctx.value.name))
    ))

  val ID = Argument("id", StringType, description = "id of the character")

  val EpisodeArg = Argument("episode", OptionInputType(EpisodeEnum),
    description = "If omitted, returns the hero of the whole saga. If provided, returns the hero of that particular episode.")

  val Query = ObjectType(
    "Query", fields[CharacterRepo, Unit](
      Field("hero", Character,
        arguments = EpisodeArg :: Nil,
        deprecationReason = Some("Use `human` or `droid` fields instead"),
        resolve = (ctx) ⇒ ctx.ctx.getHero(ctx.arg(EpisodeArg))),
      Field("human", OptionType(Human),
        arguments = ID :: Nil,
        resolve = ctx ⇒ ctx.ctx.getHuman(ctx arg ID)),
      Field("droid", Droid,
        arguments = ID :: Nil,
        resolve = Projector((ctx, f) ⇒ ctx.ctx.getDroid(ctx arg ID).get))
    ))

  val StarWarsSchema = Schema(Query)
}
