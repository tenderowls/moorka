package felix

import moorka.rx.{Val, Var}
import utest.{TestSuite, TestableString, assert}
import vaska.JSAccess

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object EntriesGeneratorSuite extends TestSuite {

  val buffer = mutable.Buffer.empty[Seq[Any]]

  def clean() = if (buffer.nonEmpty) {
    buffer.remove(0, buffer.length)
  }

  implicit val felixSystem = {
    val system = new FelixSystem {
      val ec: ExecutionContext = utest.ExecutionContext.RunNow
      val jsAccess: JSAccess = new JSAccess {
        implicit val executionContext: ExecutionContext = ec

        def send(args: Seq[Any]): Unit = buffer += args
      }
    }
    clean()
    system
  }

  val tests = TestSuite {
    "check system initialization" - {
      felixSystem.global
      felixSystem.document
      felixSystem.utils
      val length = buffer.length
      assert(length == 2)
      utest.assertMatch(buffer.head) { case Seq(_, "getAndSaveAs", "@link:global", "document", "^document") ⇒ }
      utest.assertMatch(buffer(1)) { case Seq(_, "getAndSaveAs", "@link:global", "Felix", "^Felix") ⇒ }
    }
    "check element generation" - {
      clean()
      val _ = 'div(
        'span('name /= "firstSpan"),
        "Hello",
        'mySpan('className := "secondSpan", 'disabled attr)
      )
      type S = String
      utest.assertMatch(buffer) {
        case Seq(
        Seq(_, "callAndSaveAs", "@link:^Felix", "createElementAndSetId", _, "span", span1: S),
        Seq(_, "call", setAttributeLink: S, "setAttribute", "name", "firstSpan"),
        Seq(_, "registerCallback", _),
        Seq(_, "callAndSaveAs", "@link:^Felix", "createElementAndSetId", _, "my-span", span2: S),
        Seq(_, "set", setPropertyLink: S, "className", "secondSpan"),
        Seq(_, "call", _, "setAttribute", "disabled"),
        Seq(_, "callAndSaveAs", "@link:^Felix", "createElementAndSetId", _, "div", div: S),
        Seq(_, "call", "@link:^Felix", "appendChildren", appendLink: S, Seq(fcLink: S, "Hello", scLink:S)))
          if appendLink.endsWith(div) &&
             setAttributeLink.endsWith(span1) &&
             setPropertyLink.endsWith(span2) &&
             fcLink.endsWith(span1) &&
             scLink.endsWith(span2) ⇒
      }
    }
    "check Rx attributes" - {
      val rxName = Var("name1")
      val rxClass = Var(Option.empty[String])
      val element = 'div(
        'name /= rxName,
        'class /== rxClass
      )
      clean()
      rxName.pull(Val("name2"))
      rxClass.pull(Val(Some("hello")))
      rxClass.pull(Val(None))
      utest.assert(buffer.length == 3)
      utest.assertMatch(buffer.head) {
        case Seq(_, "call", link: String, "setAttribute", "name", "name2")
          if link.endsWith(element.ref.id) ⇒ ()
      }
      utest.assertMatch(buffer(1)) {
        case Seq(_, "call", link: String, "setAttribute", "class", "hello")
          if link.endsWith(element.ref.id) ⇒ ()
      }
      utest.assertMatch(buffer(2)) {
        case Seq(_, "call", link: String, "setAttribute", "class")
          if link.endsWith(element.ref.id) ⇒ ()
      }
    }
    "check Rx properties" - {
      val rxName = Var("name1")
      val element = 'div('name := rxName)
      clean()
      rxName.pull(Val("name2"))
      utest.assert(buffer.length == 1)
      utest.assertMatch(buffer.head) {
        case Seq(_, "set", link: String, "name", "name2")
          if link.endsWith(element.ref.id) ⇒ ()
      }
    }
  }
}
