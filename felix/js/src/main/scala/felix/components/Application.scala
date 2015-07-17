package felix.components

import felix.core.FelixSystem
import felix.vdom.Element
import vaska.{JSAccess, JSObj}

import scala.scalajs.js.annotation.{JSExport, JSExportDescendentObjects}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExportDescendentObjects
trait Application {

  def start(implicit system: FelixSystem): Element

  @JSExport
  def main(jsa: JSAccess): Unit = {
    val system = new FelixSystem {
      val jsAccess = jsa
      val ec = scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
    }
    implicit val ec = system.ec
    system.document.getAndSaveAs("body", "startupBody") foreach { body ⇒
      body.call[JSObj]("appendChild", start(system).ref) foreach { _ =>
        jsa.request[Unit]("init") foreach { _ ⇒
          body.free()
        }
      }
    }
  }
}
