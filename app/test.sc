import models.Stream
import play.api.libs.json
import reactivemongo.bson.BSONObjectID

1 to 100 map { e =>
  val id = BSONObjectID.generate.stringify
  val s = Stream(id,)
}