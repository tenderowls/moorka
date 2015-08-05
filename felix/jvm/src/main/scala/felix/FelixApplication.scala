package felix

import scala.concurrent.Future

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait FelixApplication extends Component {

  def beforeStart(): Future[Unit] = {
    Future.successful(())
  }
}
