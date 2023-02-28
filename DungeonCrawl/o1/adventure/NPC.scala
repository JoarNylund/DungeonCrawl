package o1.adventure

import scala.collection.mutable.Map

/** Added Non-Player Characters.
  * NPCs have names, descriptions, a Vector of 4 lines that they can say
  * They also have traits:
  * dangerous (which can be used to determine a task, if they were but no longer are dangerous they have been calmed and allow players to pass)
  * alive (determine a task)
  * strength and wisdom scores to be compared with the players when trying to bribe/calm or attack
  * a specific item they want, that will calm them down
 * */

/** lines: what they respond to PC talking: 1) if non-dangerous from start 2) when dangerous 3) when calmed 4) when killed */
class NPC(val name:String,val description:String,line:Vector[String],var heldItems: Map[String,Item], val dangerous:Boolean, var alive:Boolean,val strength:Int,val wisdom:Int, val itemWanted:Item) {
  var calm = heldItems.contains(itemWanted.name)

  def kill() = this.alive = false

  /** the responses when a player calls the talk method. Automatically responds in other situations. */
  def respond = {
    if (!alive) line(3) else if (this.calm != dangerous) { //If dead line(3), if unchanged mind check:
      if (this.calm) line(0) else line(1)       // if it was and still is calm line(0) if it was and still is angry line(1)
   } else if (this.calm) line(2) else line(1) // but if it has changed mind and it is now calm line(2) and if it changed mind to angry line(1)
  }

}
