import enumeratum.values.StringEnumEntry
import enumeratum.values.ValueEnum
import reactivemongo.api.bson.BSONHandler
import reactivemongo.api.bson.BSONString
import reactivemongo.api.bson.BSONValue

import scala.util.Failure
import scala.util.Success
import scala.util.Try

class EnumHandler[Entry <: StringEnumEntry](enumeration: ValueEnum[String, Entry]) extends BSONHandler[Entry] {

  override def readTry(bson: BSONValue): Try[Entry] = bson match {
    case BSONString(str) => enumeration.withValueEither(str).toTry
    case value           => Failure(new IllegalArgumentException(s"$value is an invalid value for a $enumeration object"))
  }

  override def writeTry(t: Entry): Try[BSONValue] = {
    if (enumeration.values.contains(t)) {
      Success(BSONString(t.value))
    } else {
      Failure(new IllegalArgumentException(s"$t is an invalid value for a $enumeration object"))
    }

  }
}