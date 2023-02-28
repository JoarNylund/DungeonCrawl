package o1.adventure

import scala.collection.mutable.Map

/** A container class has been added. It represents searchable objects that can hold items and are found in most areas.
  * the method search in class Player calls this search method
  * once searched, a container becomes the rooms active container and the method get in class Player can pull items from it
  * @param name the name of the Container
  * @param content a Map of the items and their names that the container holds.
  * */

class Container(val name:String, var content:Map[String,Item]) {

  def search:String = {
    if (content.nonEmpty)
      "You search the " + name + ", and you find: " + content.keys.mkString(", ")
      else "You search the " + name + ", but all you find is some stale bread and a sad spider."
    }

}
