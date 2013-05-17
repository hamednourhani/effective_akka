package org.jamieallen.effectiveakka.pattern.extra

import akka.testkit.{ TestKit, TestProbe, ImplicitSender }
import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import org.scalatest.WordSpecLike
import org.scalatest.matchers.MustMatchers
import scala.concurrent.duration._
import org.jamieallen.effectiveakka.common._

class ExtraFinalSpec extends TestKit(ActorSystem("TestAS")) with ImplicitSender with WordSpecLike with MustMatchers {
  "An AccountBalanceRetriever" should {
    "return a list of account balances" in {
      val savingsAccountsProxy = system.actorOf(Props[SavingsAccountsProxyStub])
      val checkingAccountsProxy = system.actorOf(Props[CheckingAccountsProxyStub])
      val moneyMarketAccountsProxy = system.actorOf(Props[MoneyMarketAccountsProxyStub])
      val probe = TestProbe()

      val accountBalanceRetriever = system.actorOf(Props(new AccountBalanceRetrieverFinal(savingsAccountsProxy, checkingAccountsProxy, moneyMarketAccountsProxy)))
      accountBalanceRetriever.tell(GetCustomerAccountBalances(1L), probe.ref)
      val result = probe.expectMsgType[AccountBalances]
      result must equal(AccountBalances(Some(List((3, 15000))), Some(List((1, 150000), (2, 29000))), Some(List())))
    }

    "return a TimeoutException when timeout is exceeded" in {
      val savingsAccountsProxy = system.actorOf(Props[TimingOutSavingsAccountProxyStub])
      val checkingAccountsProxy = system.actorOf(Props[CheckingAccountsProxyStub])
      val moneyMarketAccountsProxy = system.actorOf(Props[MoneyMarketAccountsProxyStub])
      val probe = TestProbe()

      val accountBalanceRetriever = system.actorOf(Props(new AccountBalanceRetrieverFinal(savingsAccountsProxy, checkingAccountsProxy, moneyMarketAccountsProxy)))
      accountBalanceRetriever.tell(GetCustomerAccountBalances(1L), probe.ref)
      probe.expectMsgType[AccountBalanceRetrieverFinal.AccountRetrievalTimeout.type]
    }
  }
}