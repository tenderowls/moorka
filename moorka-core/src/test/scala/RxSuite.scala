import moorka.rx._
import utest._

import scala.concurrent.Promise
import scala.util.Success

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object RxSuite extends TestSuite {

  val tests = TestSuite {

    "check kill behavior" - {
      val emitter = Var(10)
      var calls = 0
      val cb = emitter foreach { _ ⇒
        calls += 1
      }
      cb.kill()
      emitter.pull(1)
      assert(calls == 1)
    }

    "check standard combinators" - {
      "map() should change type of value" - {
        val emitter = Var(42)
        val result = emitter.map(_.toString)
        var calls = 0
        result foreach { x ⇒
          assert(x == "42")
          calls += 1
        }
        assert(calls == 1)
      }
      "filter() should drop value not satisfied `f`" - {
        var calls = 0
        val ch = Channel[Int]()
        ch.filter(_ > 0) foreach { x ⇒
          calls += 1
          assert(x == 1)
        }
        ch.pull(0)
        ch.pull(1)
        assert(calls == 1)
      }
    }

    "check autokill combinators" - {
      "once() should kill after fist call" - {
        var calls = 0
        val ch = base.Channel[Int]()
        ch once { x ⇒
          calls += 1
        }
        ch.pull(1)
        ch.pull(2)
        ch.pull(3)
        assert(calls == 1)
      }
      "until() should kill after f returns false" - {
        var calls = 0
        val ch = Channel[Int]()
        ch until { x ⇒
          calls += 1
          x < 2
        }
        ch.pull(1)
        ch.pull(2)
        ch.pull(3)
        assert(calls == 2)
      }
    }
    
    "check flatMap behavior" - {
      "zip two channels" - {
        val state1 = Channel[String]()
        val state2 = Channel[Int]()
        val zipped = state1 zip state2
        var calls = 0
        zipped foreach { x =>
          calls += 1
          calls match {
            case 1 => assert(x.toString() == "(Cat,1)")
            case 2 => assert(x.toString() == "(Dog,1)")
          }
        }
        state1.pull("Cat")
        state2.pull(1)
        state1.pull("Dog")
        state2.pull(1)
        assert(calls == 2)
      }
      "zip two vars" - {
        val state1 = Var("Cat")
        val state2 = Var(1)
        val zipped = state1 zip state2
        var calls = 0
        zipped foreach { x =>
          calls += 1
          calls match {
            case 1 => assert(x.toString() == "(Cat,1)")
            case 2 => assert(x.toString() == "(Dog,1)")
            case 3 => assert(x.toString() == "(Dog,2)")
            case 4 => assert(x.toString() == "(Cow,2)")
            case 5 => assert(x.toString() == "(Cow,3)")
          }
        }
        state1.pull("Dog")
        state2.pull(2)
        state1.pull("Cow")
        state2.pull(3)
        assert(calls == 5)
      }
      "bind 3 channels" - {
        val ch1 = Channel[String]()
        val ch2 = Channel[String]()
        val ch3 = Channel[String]()
        var calls = 0
        ch1 flatMap { v1 ⇒
          ch2 flatMap { v2 ⇒
            ch3 map { v3 ⇒
              calls += 1
              s"$v1 $v2 $v3"
            }
          }
        } foreach { x ⇒
          assert(x == "I am cow")
        }
        ch1.pull("I")
        ch2.pull("am")
        ch3.pull("cow")
        assert(calls == 1)
      }

      "bind two vars and channel" - {
        var calls = 0
        var evalCalls = 0
        val vx = Var(Lazy(0))
        val vy = Var(Lazy(0))
        val click = Channel[Lazy[Int]]()
        val res = Channel[Lazy[Int]]()
        res <<= {
          vx >>= { x ⇒
            vy >>= { y ⇒
              click >>= { z ⇒
                Lazy {
                  evalCalls += 1
                  x() + y() + z()
                }
              }
            }
          }
        }
        val tmp = res foreach { x ⇒
          calls += 1
          assert(x() == 6)
        }
        vy() = Lazy(2)
        vx() = Lazy(2)
        System.gc()
        click.pull(Lazy(2))
        assert(calls == 1)
        assert(evalCalls == 1)
      }
    }
    
    // todo check withFilter
    "check for-comprehension" - {
      var calls = 0
      val vx = Var(2)
      val vy = Var(2)
      val res = Channel[Int]()
      res foreach { x ⇒
        calls += 1
        assert(x == 4)
      }
      res pull {
        for (
          x ← vx;
          y ← vy
        ) yield {
          x + y
        }
      }
      assert(calls == 1)
    }
    
    "check drop()" - {
      var calls = 0
      val ch = Channel[Int]()
      ch.drop(2) foreach { x ⇒
        calls += 1
        assert(x == 2)
      }
      System.gc()
      ch.pull(0)
      ch.pull(1)
      ch.pull(2)
      assert(calls == 1)
    }

    "check take()" - {
      val x = Channel[Int]()
      val res = x.take(3) foreach { x =>
        assert(x == Seq(1,2,3))
      }
      System.gc()
      x.pull(1)
      x.pull(2)
      x.pull(3)
      x.pull(4)
    }

    "check fold() on Var" - {
      val x = Var("I")
      val res = x.fold("")(_ + " " + _)
      System.gc()
      x.pull("am")
      x.pull("cow")
      res.foreach { x =>
        assert(x == " I am cow")
      }
    }

    "check fold() on Channel" - {
      val x = Channel[String]()
      val res = x.fold("")(_ + " " + _)
      x.pull("I")
      x.pull("am")
      x.pull("cow")
      res.foreach { x =>
        assert(x == " I am cow")
      }
    }

    "check future conversion" - {
      var calls = 0
      val p = Promise[Int]()
      val rx = p.future.toRx
      rx foreach { x ⇒
        calls += 1
        assert(x == Success(10))
      }
      p.success(10)
      assert(calls == 1)
    }
    
    // todo check GC behaviour
  }
}
