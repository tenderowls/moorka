package felix.dsl

import felix.core.FelixSystem
import felix.specials.ElementsPipe
import felix.vdom.NodeLike
import moorka.death.Reaper
import moorka.rx.Rx

final class NodeLikeOps(val self: NodeLike) extends AnyVal {

  def `<<:`(pipe: Rx[NodeLike])(implicit system: FelixSystem, reaper: Reaper): NodeLike = {
    val directive = new ElementsPipe(ElementsPipe.Left, pipe, system)
    directive.affect(self)
    reaper.mark(directive)
    self
  }

  def `:>>`(pipe: Rx[NodeLike])(implicit system: FelixSystem, reaper: Reaper): NodeLike = {
    val directive = new ElementsPipe(ElementsPipe.Right, pipe, system)
    directive.affect(self)
    reaper.mark(directive)
    self
  }
}
