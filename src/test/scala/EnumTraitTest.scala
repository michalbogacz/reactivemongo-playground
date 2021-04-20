
import enumeratum.values.StringEnum
import enumeratum.values.StringEnumEntry
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import reactivemongo.api.AsyncDriver
import reactivemongo.api.MongoConnection
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.MacroOptions.UnionType
import reactivemongo.api.bson.MacroOptions.Verbose
import reactivemongo.api.bson.MacroOptions.\/
import reactivemongo.api.bson.Macros
import reactivemongo.api.bson.collection.BSONCollection

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class EnumTraitTest extends AnyFlatSpecLike with Eventually with ScalaFutures with Matchers {

  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = 10.seconds,
      interval = 200.millis
    )

  sealed trait Color {
    def value: String
  }

  case class UnknownColor(value: String) extends Color

  sealed abstract class KnownColor(val value: String) extends StringEnumEntry with Color

  object KnownColor extends StringEnum[KnownColor] {
    override def values: IndexedSeq[KnownColor] = findValues

    case object Red extends KnownColor("red")

    case object Blue extends KnownColor("blue")
  }

  case class ColorWrapper(
    id: UUID,
    color: Color
  )

  implicit val knownColorFormat = new EnumHandler[KnownColor](KnownColor)
  implicit val unknownColorFormat = Macros.handler[UnknownColor]

  type PredefinedColor = UnionType[KnownColor \/ UnknownColor] with Verbose
  implicit val predefinedColor = Macros.handlerOpts[Color, PredefinedColor]
  implicit val colorWrapperHandler = Macros.handler[ColorWrapper]

  it should "read and write" in {
    val wrapper = ColorWrapper(UUID.randomUUID(), KnownColor.Red)

    val driver = AsyncDriver()
    val parsedUri = MongoConnection.fromString("mongodb://localhost:27017")
    val client = parsedUri.flatMap(driver.connect(_))

    val colorWrapperCollection: Future[BSONCollection] =
      client
        .flatMap(_.database("db"))
        .map(_.collection("coll"))

    // ends with error: The future returned an exception of type: reactivemongo.api.bson.exceptions.HandlerException, with message: Fails to handle 'color': Value doesn't match: Red.
    colorWrapperCollection
      .flatMap(_.insert.one(wrapper))
      .futureValue

    val result = colorWrapperCollection.flatMap(
      _.find(BSONDocument("id" -> wrapper.id))
        .cursor[ColorWrapper]()
        .collect[List](maxDocs = 1)
        .map(_.headOption)).futureValue


    result shouldBe Some(wrapper)

    driver.close(5.seconds).futureValue

  }
}
