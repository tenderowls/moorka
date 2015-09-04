package felix.components

import felix._
import felix.vdom.Component

object EmptyComponent {

  def apply()(implicit system: FelixSystem) = new EmptyComponent().start
}

/**
 * Represent empty DOM component
 *
 * @param system FelixSystem
 */
class EmptyComponent()(implicit val system: FelixSystem) extends Component {

  override def start = 'div('style /= "display: none;")
}
