package com.bova.app.actors

import java.util.UUID

case class BankId(idNumber : Int) {
  if(idNumber < 0) throw new RuntimeException(s"idNumber $idNumber is invalid")
}
case class MoneyRequest(money : Double)
case class MoneyResponse(money : Double)
case class Transaction (transactionUUID : UUID, sender : BankId, receiver : BankId, money : Double)
case class MoneyDeposit (money : Double)