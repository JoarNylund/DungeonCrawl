package o1.adventure


/** Item has been completely rewritten and is now a trait. Several classes that extends it has been added. Each works differently
  * when Player class method "use" is called on them
  * some of the Item Classes required access to the player and a possible target.*/
trait Item {
  def use(player:Player,target:Option[NPC]):(Boolean,String)
  val description:String
  val name:String

}

/** Carried items are those without any special effect when used
  * Could be Giveable but would cause suicide if the player has lower wisdom than the possible npc */
class Carried(val name:String, val description:String) extends Item {
  def use(a:Player,b:Option[NPC]):(Boolean,String) = (false,"You should simply hold on to this!")
}

/** Giveable items can be used to bribe/calm npcs. Each NPC has a specific item they need to be given.
  * If the NPC has a higher wisdom than the player, the NPC will exploit this to surprise attack and kill the player
  * NPCs give something back if they have, otherwise the player will get love and affection.*/
class Giveable(val name:String,val description:String) extends Item {
  override def use(player: Player,target:Option[NPC]):(Boolean,String) = {
    target match {
      case Some(npc) =>
        if (npc.alive) {
        if (player.attributes("wisdom") < npc.wisdom) {
          player.isDead = true
          (false,s"The ${npc.name} fool you and attack when you don't expect it. You better wise up!")
        } else {
        npc.heldItems += name -> this
        player.carriedItems -= name
         if (npc.itemWanted==this) {
           val reward = npc.heldItems.values.find(_ != npc.itemWanted).getOrElse(new Carried("love and affection","It's forever yours"))
           player.carriedItems += (reward.name -> reward)
           npc.calm = true
          (npc.calm, s"${npc.respond}\nYou give the ${npc.name} the ${this.name}.\nIn return, they give you a: ${reward.name}.")
        } else (npc.calm,s"${npc.respond}\nThe ${npc.name} accept the ${this.name}, but they are still angry")
        }
        } else (false,s"The ${npc.name} are no longer in a state to accept ${this.name}...")
      case None => (false,s"There's no one here to give the ${this.name} to!")
    }
  }
}

/** Weapon class items are required to kill NPCs. The player starts with fists that give no bonus in combat.
  * More can be found in the adventure and is easily added
  * To kill an NPC the players strength + weapon bonus needs to equal the NPCs.
  * An attack is made by the Player class method "use" on a Weapon class Item */

class Weapon(val name:String, val description:String,val power:Int) extends Item {
  override def use(player:Player,target:Option[NPC]):(Boolean,String) = {
    if (target.nonEmpty) {
      val actualTarget = target.get
      val actualStr = player.attributes("strength")+this.power
      if (actualStr >= actualTarget.strength) {
        actualTarget.kill()
        (true,s"${actualTarget.respond}\nYou still had some strength in you! The ${actualTarget.name} is now dead and you may proceed!")
    } else {
        player.isDead = true
        (false,"You tried to attack, but the odds weren't in your favor...")
    }
    }
    else (false,"Who would you attack? Imagining things again are we?")
  }
}

/** Activatable items are those that have a single use available.
  * Player class method "use" will complete a task if the task required this specific item.
  * if the item is a torch, it grants the player light and lights up the room. */

class Activatable(val name:String,val description:String) extends Item {

  override def use(player:Player,target:Option[NPC]):(Boolean,String) = {
    val actualTask = player.location.task
    if (actualTask.isInstanceOf[ItemRequired]) {
      if (actualTask.attempt(player.carriedItems,target,Some(this),player))
        (true,s"Good thing you had ${this.name} on you!")
      else
        (false, s"You're gonna need a different item than ${this.name}here...")
    } else if (this.name == "torch") {
      player.hasLight = true
      player.location.lightUp()
      (false,"You light a torch. Good choice!")
    }
    else (false,"No such items are needed here.")
  }
}


/** Teleport is made for the artifact so the game can be completed a bit quicker instead of going through all the rooms.
  * Calls the Player class method teleport, which takes the player back to the entrance where the game will complete. */
class Teleport(val name:String, val description:String) extends Item {
  def use(player: Player,target:Option[NPC]) = {
    (true,player.teleport)
  }
}
