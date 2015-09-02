package felix

import utest._
import vaska.JSAccess

import scala.concurrent.ExecutionContext

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object TwoWayBindingSuite extends TestSuite {

  def mock() = {
    val ec = utest.ExecutionContext.RunNow
    val system = new FelixSystem {
      val executionContext: ExecutionContext = ec
      val jsAccess: JSAccess = new JSAccess {
        def send(args: Seq[Any]): Unit = ???
        implicit val executionContext: ExecutionContext = ec
      }
    }
  }
  val tests = TestSuite {
  }
}
