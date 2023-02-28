package o1.adventure

import scala.collection.mutable.Map


/** A `Player` object represents a player character controlled by the real-life user of the program.
  * This class has been modified heavily to allow for the additions.
  * A player now has the attributes strength & wisdom to be decided upon start, allowing for different paths to complete the adventure.
  * If a player carries a lit torch they have light and will see the rooms and be able to access their contents.
  * A player kan get killed.
  *
  * @param startingArea  the initial location of the player */
class Player(startingArea: Area) {

  private var currentLocation = startingArea        // gatherer: changes in relation to the previous location
  private var lastLoc = startingArea
  private var quitCommandGiven = false              // one-way flag
  val fists = new Weapon("fists","your own fists. I guess they could do in a pickle.",0)
  val torch = new Activatable("torch", "an unlit torch.\n'Could be useful'")
  var carriedItems = Map[String,Item](fists.name -> fists,torch.name -> torch)
  var isDead = false
  var hasLight = false


  /** Attributes that will be modified when game is created */
  var attributes = Map[String,Int]("strength" -> 0,"wisdom"-> 0)  // Decides how smart, charismatic a PC is - used to bribe,deceive,figure challenges out?
  var points = 3

  /** New: tracks if the player has died */
  def hasDied = this.isDead

  /** Determines if the player has indicated a desire to quit the game. */
  def hasQuit = this.quitCommandGiven

  /** Returns the current location of the player. */
  def location = this.currentLocation
  /** New: Returns the previous location. Used in AdventureGUI method updateInfo and modified by "go" method */
  def lastLocation = lastLoc



  /** UNMODIFIED METHODS */

    /** Causes the player to rest for a short while (this has no substantial effect in game terms).
    * Returns a description of what happened. */
  def rest() = {
    if (this.currentLocation.name == "Button Room") "Resting in a time like this? You better figure something out..."  else "You rest for a while. Better get a move on, though."
  }

  /** Signals that the player wants to quit the game. Returns a description of what happened within
    * the game as a result (which is the empty string, in this case). */
  def quit() = {
    this.quitCommandGiven = true
    ""
  }

    def examine(itemName:String):String = {
    if (this.carriedItems.contains(itemName)) {
      s"You look closely at the ${itemName}.\n${carriedItems(itemName).description}."
    } else {
      "If you want to examine something, you need to pick it up first."
    }
  }

  def inventory:String = {
    if (this.carriedItems.isEmpty) {
      "You are empty-handed"
    } else {
      s"You are carrying:\n${this.carriedItems.keys.mkString("\n")}"
    }
  }

  def has(itemName:String):Boolean = {
    this.carriedItems.contains(itemName)
  }



  /** MODIFIED METHODS */

  /** Modified: if a player hasLight (carries a lit torch) it lights up the next room as well.
    * Also checks if a player completes the passive task attReq(attribute requirement) to open the tasked exits instantly or not
    * Attempts to move the player in the given direction. This is successful if there
    * is an exit from the player's current location towards the direction name. Returns
    * a description of the result: "You go DIRECTION." or "You can't go DIRECTION." */
  def go(direction: String) = {
    lastLoc = this.currentLocation
    var destination = this.currentLocation.neighbor(direction)
    if (destination.isEmpty && this.currentLocation.taskCompleted) destination = this.currentLocation.taskedNeigbor(direction)
    this.currentLocation = destination.getOrElse(this.currentLocation)
    if (this.hasLight) this.currentLocation.lightUp()
    if (this.currentLocation.task.isInstanceOf[attReq]) this.currentLocation.task.attempt(carriedItems,None,None,this)
    if (this.currentLocation != lastLoc) "You go " + direction + "."
    else "You can't go " + direction + "."
  }

  /** Modified: searches ground then the possible opened (searched) container.
   *  1) Is the item on the ground? If so get it
   *  2) Do we have an active container? if so -> 3)
   *  3) Is the item in the active container? if so get it  */
  def get(itemName:String):String = {
    if (this.currentLocation.contains(itemName)) {
      this.carriedItems += itemName -> this.currentLocation.removeItem(itemName).get
      s"You pick up the $itemName."
    } else {
      if (this.currentLocation.activeContainer.nonEmpty) {
        val container = currentLocation.activeContainer.get
        container.content.find(_._1 == itemName) match {
          case Some((string,item)) =>
            this.carriedItems += string -> item
            container.content -= string
            s"You pick up the $string from the ${container.name}"
          case None => "There is no " + itemName + " to pick up here."
        }
      } else "There is no " + itemName + " to pick up here."
    }
  }

 /** Modified to first check if there is an open container in the room to drop the item into.
   * Also modified so that a torch that gets dropped flickers out.
   * Otherwise unchanged */

  def drop(itemName:String):String = {
    var extra:String = ""
    if (this.carriedItems.contains(itemName)) {
      if (itemName == "torch") {
        this.hasLight = false
        this.currentLocation.isDark = true
        extra = " And it flickers out. Better light it again if you pick it up!"
      }
      if (this.currentLocation.activeContainer.nonEmpty) {
        this.currentLocation.activeContainer.get.content += itemName -> carriedItems(itemName)
        this.carriedItems -= itemName
        s"You drop the $itemName." + extra
      } else {
      this.currentLocation.addItem(carriedItems(itemName))
      this.carriedItems -= itemName
      s"You drop the $itemName." + extra
    }
    } else "You don't have that!"
  }


  /** COMPLETELY NEW COMMANDS */


  /** method to use a carried item. The actual usage is defined by the item type. */
  def use(itemName:String):String = {
    val actualItem      = if(this.carriedItems.contains(itemName)) this.carriedItems.get(itemName) else None
    val possibleTarget  = this.currentLocation.person
    actualItem match {
      case Some(item) =>
        if (item.use(this,possibleTarget)._1) {
          this.currentLocation.completeTask()
        }
        item.use(this,possibleTarget)._2
      case None => "That's not an item you have.."
    }
  }

  /** Method to alter the attributes before starting the adventure
    * The player has 3 available points to spread out as they wish between strength and wisdom, leading to different playstyles.
    * The player can only modify the attributes in the entrance, before leaving it the first time*/

  def increase(attribute:String) = {
    if (this.currentLocation == this.lastLocation) {
    if (points < 1) "Your character is at it's full power already, get to adventuring now!"
    else if (attributes.keys.exists(_ == attribute) && (points > 0)) {
     attributes.update(attribute,attributes(attribute)+1)
     points -= 1
     this.currentLocation.task.attempt(carriedItems,None,None,this)
     "You are becoming more powerful. " + attribute + " is now increased to " + s"${attributes(attribute).toString}"
    } else "not a valid attribute"
  } else "You can no longer decide your attributes. Get on with it instead!"
  }

  def decrease(attribute:String) = {
    if (this.currentLocation == this.lastLocation) {
    if (attributes.keys.exists(_ == attribute) && attributes(attribute)>0) {
      attributes.update(attribute,attributes(attribute)-1)
      points += 1
      attribute + " is decreased to " + s"${attributes(attribute).toString}." + " You have one more increase available"
    } else "The attribute is already at 0, you can't decrease it!"
    } else "You can no longer decide your attributes. Get on with it instead!"
  }


  /** Searches a container and gives it's contents as a string.
    * The search method sets the container as active(open) and calls its own search method, producing a string
    * The get method can take items from the container now that it has been set active
    * Only works if the player has a lit torch (or is at the entrance, which is bright by itself) */
  def search(containerName:String):String = {
    if (this.currentLocation.name == "Entrance" || this.hasLight) {
      this.currentLocation.activeContainer = this.currentLocation.container.find(_.name == containerName)
      this.currentLocation.activeContainer match {
        case Some(container:Container) =>
          container.search
        case None => "There is no " + containerName + " here! Are you sure your torch is working?..."
      }
    } else s"I can't see anything! Is there even a $containerName here?"
  }

  /** method to talk with NPCs. Will call their respond method */
  def talk:String = {
   if (this.currentLocation.person.nonEmpty)
    this.currentLocation.person.get.respond
   else "There's no one here! Have you been down here for too long?"
  }

  /** Attemps to loot the NPC in the room.
    * If there is one all possible items are added to the players inventory */
  def lootBody:String = {
    val target = this.currentLocation.person
    val lootedItems = Map[String,Item]()
    if (target.nonEmpty && !target.get.alive) {
      for (item <- target.get.heldItems) {
        lootedItems += item
        this.carriedItems += item
      }
      s"You loot the ${target.get.name} and add a ${lootedItems.keys.mkString("and a")} to your inventory"
    } else "There's no one around to loot..."
  }

  /** method to close the open container. Useful to drop an item on to the ground in stead of into container */
  def close():String = {
    if (this.currentLocation.activeContainer.nonEmpty) {
      val name = this.currentLocation.activeContainer.get.name
      this.currentLocation.activeContainer = None
      "You close the " + name + "."
    } else {
    this.currentLocation.activeContainer = None
      "There are no opened  containers here"
    }
  }

   /** a method added so the artifact can take a player instantly back to the beginning/end */
  def teleport:String = {
    this.currentLocation = startingArea
    "You successfully teleport to: " + startingArea.name
  }

  /** method added for the Scam task in the timerRoom
    * activates the countdown that will lead to nothing (but stress) */
  def push:String = {
    if (currentLocation.task == Scam) {
      currentLocation.task.turnsLeft = 3
      s"'Click'\nA torch goes out. The ground is shaking.\n${location.task.turnsLeft.toString} are still lit.\nWhat happens when you're down to 0?"
    } else "There's nothing to push here?"
  }


  def help:String = {
  "A little bit of help on how to play.\n\nYour goal is to reach the end of the dungeon, where you will find an item called artifact.\nThe artifact can teleport you back to the entrance and end the game."+
  "\n\nThere are NPCs that needs to be either calmed or killed, both by using items that you find in the dungeon. Weapons for fighting, items to bribe with! \nYour attributes matter, more strength means you can fight your way through and more wisdom the other way" +
  "\n\nUseful commands:" +
    "\n\nsearch 'name of container': Searches the containers in the room. You can then use Get to pick up items from them."+
    "\nget 'name of item': Picks up the item described"+
    "\nuse 'name of item': Uses the item described, effect will depend on item. Weapons make you attack, Giveable objects makes you bribe an npc, Activateable objects does what is logical for the name e.g. 'use torch' lights it"+
    "\ntalk: Makes the NPC in the room respond with a line of text. Never dangerous." +
    "\nloot: Makes you loot any dead NPCs in the room and adds their items to your inventory. Remember to do this after using your weapons to attack"+
    "\nexamine 'name of item': Gives a more detailed description of the item." +
    "\npush: Makes you push a button, if there is any in the room. Will be necessary for a certain room." +
    "\n\nRECAP: Light a torch by writing 'use torch', do an attack by writing 'use 'name of weapon'', bribe NPCs by writing 'use 'name of bribe'' \n\nAdd your attributes before you can continue"
  }

  /** Returns a brief description of the player's state, for debugging purposes. */
  override def toString = "Now at: " + this.location.name


}


