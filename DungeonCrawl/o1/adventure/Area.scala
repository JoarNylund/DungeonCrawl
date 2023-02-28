package o1.adventure

import scala.collection.mutable.Map

/** Area works pretty much as before, with a few additions.
  * @param name         the name of the area
  * @param description  a basic description of the area (typically not including information about items)
  * @param container    added container(s), another layer of space for items to be in. Can be searched and items in it can be taken with the same get method
  * @param person       an area can have an npc in it. The npc can be interacted with, and may be connected to the rooms task.
  * @param task         tasks are challenges that needs to be completed before unlocking all exits. Exits that require the task are collected in the Map taskedNeighbors
  * */
class Area(var name: String, var description: String,val container: Vector[Container],var person: Option[NPC],val task:Task) {


  /** If a room has a container and it has been searched it becomes the activeContainer. The get method can now pull items out of it
   * the rooms are dark(except for entrance), requiring a player to light a torch to see description, exits and search and see containers.
   * once the rooms task is completed all exits open. Areas without tasks gets the "allGood" task object */
  private val neighbors = Map[String, Area]()
  private val taskedNeighbors = Map[String,Area]()
  private val itemList = Map[String,Item]()
  var activeContainer:Option[Container] = None
  var taskCompleted = task.isCompleted
  var isDark = true

  /** the Player class method "use" on the item torch will call this method, revealing the room to the player*/
  def lightUp() = isDark = false

  /** method called after a task is completed */
  def completeTask() = this.taskCompleted = true

  /** Returns the area that can be reached from this area by moving in the given direction. The result
    * is returned in an `Option`; `None` is returned if there is no exit in the given direction. */
  def neighbor(direction: String) = this.neighbors.get(direction)
  def taskedNeigbor(direction:String) = this.taskedNeighbors.get(direction)

  /** Adds an exit from this area to the given area. The neighboring area is reached by moving in
    * the specified direction from this area. */
  def setNeighbor(direction: String, neighbor: Area) = {
    this.neighbors += direction -> neighbor
  }

  /** Adds exits from this area to the given areas. Calling this method is equivalent to calling
    * the `setNeighbor` method on each of the given direction--area pairs.
    * @param exits  contains pairs consisting of a direction and the neighboring area in that direction
    * @see [[setNeighbor]] */
  def setNeighbors(exits: Vector[(String, Area)]) = {
    this.neighbors ++= exits
  }
  def setTaskedNeighbors(exits: Vector[(String, Area)]) = {
    this.taskedNeighbors ++= exits
  }

  /** Returns a description of what the player sees. The areas description, available exits, containers (if any) and items (if any).
   *  If the player is without a lit torch they cant see anything.
   *  the starting area "Entrance" is an exception
   */
  def fullDescription:String = {
    val exits = if (this.taskCompleted) this.neighbors.keys ++ this.taskedNeighbors.keys else this.neighbors.keys
    val exitList = "\n\nExits available: " + exits.mkString(" ")
    val itemOnly = "\nLoose items: " + this.itemList.keys.mkString(" ")
    val containerOnly = "\nSearchable objects: a " + this.container.map(_.name).mkString(", a ")
    val itemAndContainer = containerOnly + "\nLoose items: a " + this.itemList.keys.mkString(", a ")

    if (!this.isDark || this.name == "Entrance" ) {
      if (this.container.nonEmpty) {
        if (this.itemList.nonEmpty) {
          description + exitList + itemAndContainer
        } else
          description + exitList + containerOnly
      } else if (this.itemList.nonEmpty) {
          description + exitList + itemOnly
      } else description + exitList
    } else "It's pitch black in here... You can barely make out the walls and any loose objects." + (if(this.itemList.nonEmpty) itemOnly else "")
  }


  /** Rest has been left untouched */

  def addItem(item:Item) = {
     this.itemList += item.name -> item
  }

  def contains(itemName:String):Boolean = {
    this.itemList.contains(itemName)
  }

  def removeItem(itemName:String):Option[Item] = {
    if (this.contains(itemName)) {
      this.itemList.remove(itemName)
    } else {
      None
    }
  }
}
