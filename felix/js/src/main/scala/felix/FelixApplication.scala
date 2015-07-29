package felix

import vaska.{JSAccess, JSObj}

import scala.scalajs.js.annotation.{JSExport, JSExportDescendentObjects}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExportDescendentObjects
trait FelixApplication extends Component {

  private var systemFromJS: FelixSystem = null

  implicit def system: FelixSystem = systemFromJS

  @JSExport
  def main(jsa: JSAccess): Unit = {
    systemFromJS = new FelixSystem {
      val jsAccess = jsa
      val ec = scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
    }
    system.document.getAndSaveAs("body", "startupBody") foreach { body ⇒
      body.call[JSObj]("appendChild", this.ref) foreach { _ =>
        jsa.request[Unit]("init") foreach { _ ⇒
          body.free()
        }
      }
    }
  }
}
