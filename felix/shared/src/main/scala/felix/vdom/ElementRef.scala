package felix.vdom

import java.util.UUID

import felix.core.FelixSystem
import vaska.JSObj

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class ElementRef(tag: String)(implicit system: FelixSystem) extends JSObj {

  import FelixSystem.utils._
  
  val jsAccess = system.jsAccess
  
  val id = UUID.randomUUID().toString

  system.utils.callAndSaveAs(CreateElementAndSetId, tag, id)(id)
}
