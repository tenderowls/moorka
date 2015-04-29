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
    setJSAccess(jsAccess) foreach { _ =>
      document.get[JSObj]("body") foreach { body ⇒
        body.call[JSObj]("appendChild", start().ref)
      }
    }
  }

}
