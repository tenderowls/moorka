package felix.dsl

import felix.core.FelixSystem
import felix.vdom.Directive
import felix.vdom.directives.UseClassDirective
import moorka.rx.Rx

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait HtmlHelpers {

  case class useClass(className: String) {
    def when(trigger: Rx[Boolean])(implicit system: FelixSystem): Directive = {
      new UseClassDirective(className, trigger, system)
    }
  }

}
