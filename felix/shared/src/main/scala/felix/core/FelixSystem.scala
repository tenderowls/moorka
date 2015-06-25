package felix.core

import java.util.UUID

import vaska.{JSAccess, JSObj}

import scala.concurrent.ExecutionContext

object FelixSystem {
  val GlobalObjectName = "global"
  val FelixObjectName = "Felix"
  val FelixObjectId = "^Felix"
  val DocumentObjectName = "document"
  val DocumentObjectId = "^document"
  
  object utils {
    val CreateElementAndSetId = "createElementAndSetId"
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait FelixSystem {

  import FelixSystem._

  val ec: ExecutionContext
  
  val jsAccess: JSAccess

  lazy val global = jsAccess.obj(GlobalObjectName)

  lazy val document = {
    global.getAndSaveAs(DocumentObjectName, DocumentObjectId)
    jsAccess.obj(DocumentObjectId)
  }

  lazy val utils = {
    global.getAndSaveAs(FelixObjectName, FelixObjectId)
    jsAccess.obj(FelixObjectId)
  }
}
