package felix

import moorka.flow.Context
import vaska.{JSAccess, JSObj}

import scala.concurrent.Future
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
      val flowContext = Context()
      val jsAccess = jsa
      val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
    }
    system.document.getAndSaveAs("body", "startupBody") foreach { body ⇒
      jsa.request[Unit]("init") foreach { _ ⇒
        beforeStart() foreach { _ ⇒
          body.call[JSObj]("appendChild", this.ref) foreach { _ =>
            body.free()
          }
        }
      }
    }
  }

  def beforeStart(): Future[Unit] = {
    Future.successful(())
  }
}
