import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.AsyncDriver
import reactivemongo.api.MongoConnectionOptions
import reactivemongo.api.MongoConnectionOptions.Credential
import reactivemongo.api.ReadConcern

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object Application extends App with StrictLogging {

  val user = ""
  val password = "=="
  val host = "test.mongo.cosmos.azure.com:10255"
  val appName = "@@"

  val maxIdleTimeMS = 120000

  val driver = AsyncDriver()
  val options = MongoConnectionOptions(
    credentials = Map(user -> Credential(user, Some(password))),
    keepAlive = true,
    sslEnabled = true,
    maxIdleTimeMS = maxIdleTimeMS,
    appName = Some(appName),
    readConcern = ReadConcern.Majority
  )

  val db =  Await.result(driver.connect(Seq(host), options).flatMap(_.database("shipment")), 20.seconds)

  while(true) {
    logger.info("Names "+Await.result(db.collectionNames, 5.seconds))
    Thread.sleep(10000) // only for test purpose
  }


}
