package repositories

import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString

private[repositories] case class Index(name: String, key: BsonDocument)

private[repositories] object Index {

  def apply(document: Document): Index =
    new Index(
      document.get[BsonString]("name").get.getValue,
      document.get[BsonDocument]("key").get
    )
}
