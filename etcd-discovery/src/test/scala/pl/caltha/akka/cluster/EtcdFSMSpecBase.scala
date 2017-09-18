package pl.caltha.akka.cluster

import akka.actor.ActorSystem
import akka.actor.FSM.{CurrentState, Transition}
import akka.testkit.{TestKit, TestProbe}
import me.maciejb.etcd.client.EtcdClient
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalatest.mock.MockitoSugar

import scala.concurrent.duration.DurationInt

abstract class EtcdFSMSpecBase[State, Data](_system: ActorSystem)
    extends TestKit(_system) with FlatSpecLike with BeforeAndAfterAll with Matchers with MockitoSugar {

  def this() =
    this(ActorSystem("testsystem"))

  def settings = ClusterDiscoverySettings.load(system.settings.config)

  def transitionTimeout = 10.seconds

  trait FixtureBase {

    val etcd = mock[EtcdClient]

    val stateProbe = TestProbe()

    def expectTransitionTo(expState: State) =
      stateProbe.expectMsgType[Transition[State]](transitionTimeout) should matchPattern {
        case Transition(_, _, state) if state == expState ⇒
      }

    def expectInitialState(expState: State) =
      stateProbe.expectMsgType[CurrentState[State]](transitionTimeout) should matchPattern {
        case CurrentState(_, state) if state == expState ⇒
      }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
