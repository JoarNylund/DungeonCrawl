package o1.adventure

import scala.collection.mutable.Map

/** A class to represent tasks required to complete in some rooms. Completing it will allow players to proceed
 * failing may result in losing the game */

trait Task {
  var isCompleted = false
  var turnsLeft = 4
  def attempt(inventory:Map[String,Item],target:Option[NPC],itemRequired: Option[Item], player:Player):Boolean
}

object allGood extends Task {
  isCompleted = true
  override def attempt(a:Map[String,Item],b:Option[NPC],c:Option[Item],d:Player):Boolean = true
}

class ItemRequired(val itemRequired: Activatable) extends Task {
  override def attempt(a:Map[String,Item],b:Option[NPC],key:Option[Item],d:Player) = {
    if (key.nonEmpty && key.get ==itemRequired) {
      this.isCompleted = true
    }
    this.isCompleted
  }
}

object KillOrCalmRequired extends Task {
  override def attempt(inventory:Map[String,Item],target:Option[NPC],c:Option[Item],d:Player):Boolean = {
    this.isCompleted = target.get.calm
    this.isCompleted
  }
}

object Scam extends Task {
  override def attempt(a:Map[String,Item],b:Option[NPC],c:Option[Item],d:Player):Boolean = {
    false
  }
  this.isCompleted = this.turnsLeft <= 0
}

class attReq(attribute:String,amount:Int,inverse:Boolean) extends Task {
  override def attempt(a:Map[String,Item],b:Option[NPC],c:Option[Item],player:Player): Boolean = {
    if (inverse) {
      if (player.attributes(attribute)<amount) {
        player.location.completeTask()
        true
      } else false
    }
    else
      if (player.attributes(attribute)>=amount) {
         player.location.completeTask()
         true
      } else false
  }
}

object modify extends Task {
  override def attempt(a:Map[String,Item],b:Option[NPC],c:Option[Item],player:Player): Boolean = {
    if (player.points == 0) {
      player.location.completeTask()
      true
    } else false
  }
}


