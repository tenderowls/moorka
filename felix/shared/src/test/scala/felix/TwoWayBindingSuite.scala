package felix

import felix.core.FelixSystem
import utest._
import vaska.JSAccess

import scala.concurrent.ExecutionContext

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object TwoWayBindingSuite extends TestSuite {

  def mock() = {
    val system = new FelixSystem {
      val ec: ExecutionContext = utest.ExecutionContext.RunNow
      val jsAccess: JSAccess = new JSAccess {
        def send(args: Seq[Any]): Unit = ???
        implicit val executionContext: ExecutionContext = ec
      }
    }
  }
  val tests = TestSuite {
  }
}
