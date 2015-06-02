package moorka.ui.components.base

import moorka.ui._
import moorka.ui.element.ElementBase
import vaska.{JSAccess, JSObj}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js.annotation.{JSExport, JSExportDescendentObjects}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExportDescendentObjects
trait Application {

  def start(): ElementBase

  @JSExport
  def main(jsAccess: JSAccess): Unit = {
    for {
      _ ← setJSAccess(jsAccess)
      body ← document.get[JSObj]("body")
      _ ← body.save()
      _ ← body.call[JSObj]("appendChild", start().ref)
      _ ← body.free()
      _ ← jsAccess.request[Unit]("init")
    } yield {
      ()
    }
  }

}
