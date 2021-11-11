package repositories

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.{FailoverStrategy, ReadPreference}
import reactivemongo.api.commands.Command
import reactivemongo.api.bson.BSONDocument
import reactivemongo.core.errors.ReactiveMongoException

import scala.concurrent.ExecutionContext.Implicits.global

trait FailOnUnindexedQueries extends MongoSuite with BeforeAndAfterAll with ScalaFutures with TestSuiteMixin {
  self: TestSuite =>

  private val commandRunner = Command.run(BSONSerializationPack, FailoverStrategy())

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    commandRunner(
      db = MongoSuite.connection.flatMap(_.database("admin")).futureValue,
      command = commandRunner.rawCommand(BSONDocument("setParameter" -> 1, "notablescan" -> 1))
    ).one[BSONDocument](ReadPreference.primaryPreferred).futureValue
  }

  override protected def afterAll(): Unit = {
    commandRunner(
      db = MongoSuite.connection.flatMap(_.database("admin")).futureValue,
      command = commandRunner.rawCommand(BSONDocument("setParameter" -> 1, "notablescan" -> 0))
    ).one[BSONDocument](ReadPreference.primaryPreferred).futureValue

    super.afterAll()
  }

  abstract override def withFixture(test: NoArgTest): Outcome =
    super.withFixture(test) match {
      case Failed(e: ReactiveMongoException) if e.getMessage() contains "No query solutions" =>
        Failed("Mongo query could not be satisfied by an index:\n" + e.getMessage())
      case thing @ Failed(e) =>
        thing

      case other =>
        other
    }
}
