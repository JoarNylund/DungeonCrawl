package o1.adventure

import scala.collection.mutable.Map

/** The class `Adventure` represents text adventure games. An adventure consists of a player and
  * a number of areas that make up the game world. It provides methods for playing the game one
  * turn at a time and for checking the state of the game.
  *
  * N.B. This version of the class has a lot of "hard-coded" information which pertain to a very
  * specific adventure game that involves a small trip through a twisted forest. All newly created
  * instances of class `Adventure` are identical to each other. To create other kinds of adventure
  * games, you will need to modify or replace the source code of this class. */
class Adventure {



  /** The title of the adventure game. */
  val title = "A dungeon crawl, filled with dangers!"

  /** Items  */
  val letter         = new Carried("letter", "looks like a page out of someones diary...\nYou read it:\n'With the beasts only friend gone, it makes a perfect guard. Good thing we locked her up!'")
  val button         = new Carried("button", "a button from someones coat")
  val bones          = new Carried("bones","a heap of goblin bones. It wasn't easy taking the doll from Bessie")
  val pebble         = new Carried("pebble","a useless little pebble")
  val tail           = new Carried("tail","a small rats tail")
  val axe            = new Weapon("axe", "a heavy, sharpened battleaxe.\n'This is not a toy.'",1)
  val sword          = new Weapon("sword", "a fine longsword, fit for a fine warrior such as yourself\nIt's in decent shape still!",1)
  val torch          = new Activatable("torch", "an unlit torch.\n'Could be useful'")
  val key            = new Activatable("key", "a silver key, now covered in blood.\n'I wonder what it unlocks...'")
  val doll           = new Giveable("doll", "a ragged smelly doll.\n'Who would want this?'")
  val bracelet       = new Giveable("bracelet", "a golden bracelet, with a large emerald embedded.\n'This piece is very valuable, no ordinary craftmanship here'")
  val coin           = new Giveable("coin", "a nice gold coin\nNot worth too much though.")
  val cheese         = new Giveable("cheese", "a small chunk of cheese.\n'Why would I keep this?'")
  val artifact       = new Teleport("artifact","The long lost artifact! the reason you entered this dungeon!\n'It's rumoured to have great magical capabilities. I wonder of what sort?'")

  /** Containers, placed into Vectors to combine with ++ when needed*/
  val empty = Vector[Container]()
  val statue = Vector(new Container("statue",Map(sword.name -> sword)))
  val rack = Vector(new Container("weaponrack", Map(axe.name -> axe)))
  val desk = Vector(new Container("desk",Map(letter.name -> letter,coin.name -> coin)))
  val crate = Vector(new Container("crate",Map(torch.name -> torch,button.name -> button)))
  val chestBracelet = Vector(new Container("chest", Map(bracelet.name -> bracelet)))
  val justBarrel = Vector(new Container("barrel", Map(torch.name -> torch)))

  /** NPCs */
  val goblinTrio       = new NPC("guarding goblins",
                                 "three green short fellows. Goblins. Greedy little creatures. They wont let you past the northern door!",
                                  Vector("Should not happen","You better got something to give us, or you'll get our steel!","Go then, and take this key!","You got us, but you'll never get past Bessie!"),
                                  Map(key.name -> key),true,true,3,2,bracelet)
  val surprisedGoblins = new NPC("surprised goblins",
                                 "two surprised goblins, sleeping on the floor. \nYou have both awoken and angered them now!\nThey stand between you and the western exit.",
                                 Vector("Should not happen","What?! How did you get past the others?\nIf you bribed them you better bribe me as well!","If you're here, they are dead or betrayed me so my consciense is clean.\nGo on","You fool. The worst is yet to come!"),
                                 Map(pebble.name -> pebble),true,true,2,3,cheese)
  val monster          = new NPC("monster",
                                 "a gruesome slimy monster that guards this room and the northern exit.",
                                 Vector("Should not happen","GROWL","The monster is overjoyed to see its friend again!\nIt walks away from the northern door and allows you through.","The only thing greater than this beast is your strength!"),
                                 Map(bones.name -> bones),true,true,4,0,doll)
  val rat              = new NPC("small rat",
                                 "A small gray rat, who squeals at you!",
                                 Vector("confused squeal","angry squeal","happy squeals","sad squeal"),
                                 Map(),true,true,0,0,cheese)

  /** Areas */

  private val entrance    = new Area(   "Entrance",
                                        "You have arrived at the entrance of the dungeon. It looks dark down there.\n'Am I ready for this?'\n\nBegin by modifying your character:\nYou have 3 points available, divide them freely to strength and wisdom by writing 'add strength' or 'add wisdom' until all 3 points are spent\nYou can then proceed north",
                                        justBarrel,None,modify)
  private val corridoor   = new Area(   "Corridoor",
                                        "You have entered the dungeon. Ahead of you lies a long, dark and drafty corridoor, with staircases going both east and west.\nAt the end of the room, you see a statue of a warrior holding a sword and pointing it to the west",
                                        statue++justBarrel,None,new attReq("wisdom",2,true))
  private val pit         = new Area(   "Pit",
                                        "You have entered the prisoner's pit",
                                        empty,None,allGood)
  private val guardroom   = new Area(   "Guardroom",
                                        "You enter a room that bears resemblance to a barrack. Only filthier than what you could imagine \nExcept for the stairs back, the only other exit is through a locked d oor.",
                                        rack++desk,None,allGood)
  private val jewelryRoom = new Area(   "Storage",
                                        "You enter a room, filled with containers to store things in. Most look empty or useless",
                                        chestBracelet,None,allGood)
  private val goblinHangout = new Area( "Hangout",
                                        s"You enter a smelly, smoky room. There is a table, a few stools and a couple of bedrolls on the floor\nSitting around the table, you see ${goblinTrio.description}.",
                                        justBarrel,Some(goblinTrio),KillOrCalmRequired)
  private val fourWay = new Area(       "Four-Way exit room",
                                        "You enter a room with a door in every direction.\nThe door leading north, however, is locked!",
                                        empty,None,new ItemRequired(key))
  private val closet = new Area(        "Closet",
                                        s"Behind the door lies a small closet. It's filled with dusty old clothes and ${rat.description}\nWhat could it want from you?",
                                        empty,Some(rat),KillOrCalmRequired)
  private val dollRoom = new Area(      "Storage",
                                        "You enter a room, filled with dusty barrels and crates. One of of them has been touched recently, on top of it lies a ragged doll",
                                         crate,None,allGood)
  private val timerRoom = new Area(     "Button Room",
                                        "You have entered a large room, completely empty except for a large button in the middle\nAll doors,including the one you entered through are suddenly locked\nYou see four lit torches on the walls.\n'Maybe I should push the button?'",
                                        empty,None,Scam)
  private val goblinQuarters = new Area("Goblin Quarters",
                                        s"A foul stench hits you. You have entered the Goblins' sleeping quarters.\nIn the middle of the mess you see ${surprisedGoblins.description}.",
                                        empty,Some(surprisedGoblins),KillOrCalmRequired)
  private val monsterRoom = new Area(   "Monster Lair",
                                        s"The goblins said the worst is yet to come. You have now entered what looks like a monsters playground.\nIndeed, ${monster.description}\nIt seems very lonely.",
                                        empty,Some(monster),KillOrCalmRequired)
  private val artifactRoom = new Area(  "Treasure Room",
                                        s"Finally. You have reached your destination in this dungeon. You see something on a pedestal. ${artifact.description}\nThe door you came through slams shut and is impossible to open",
                                        justBarrel,None,allGood)

  private val destination = entrance


  /** Neighbors + Tasked Neighbors to be unlocked + Eventual items outside of containers*/
  entrance.setTaskedNeighbors(Vector(           "north" -> corridoor                                                                                    ))
  corridoor.setNeighbors(Vector(                                    "east" -> jewelryRoom,     "south" -> entrance                                ))
  corridoor.setTaskedNeighbors(Vector(                                                                                   "west" -> pit            ))
  pit.setNeighbors(Vector(                "north" -> guardroom                                                                                    ))
  guardroom.setNeighbors(Vector(                                    "east" -> goblinHangout,   "south" -> pit                                     ))
  jewelryRoom.setNeighbors(Vector(        "north" -> goblinHangout,                                                      "west" -> corridoor      ))
  goblinHangout.setNeighbors(Vector(                                                           "south" -> jewelryRoom,   "west" -> guardroom      ))
  goblinHangout.setTaskedNeighbors(Vector("north" -> fourWay                                                                                      ))
  fourWay.setNeighbors(Vector(                                      "east" -> closet,          "south" -> goblinHangout, "west" -> timerRoom      ))
  fourWay.setTaskedNeighbors(Vector(      "north" -> dollRoom                                                                                     ))
  fourWay.addItem(cheese)
  closet.setNeighbors(Vector(                                                                                            "west" -> fourWay        ))
  dollRoom.setNeighbors(Vector(                                                                 "south" -> fourWay                                ))
  dollRoom.addItem(doll)
  timerRoom.setTaskedNeighbors(Vector(    "north" -> goblinQuarters, "east" -> fourWay                                                            ))
  goblinQuarters.setNeighbors(Vector(                                                           "south" -> timerRoom                              ))
  goblinQuarters.setTaskedNeighbors(Vector(                                                                              "west" -> monsterRoom    ))
  monsterRoom.setNeighbors(Vector(                                                                                       "west" -> goblinQuarters ))
  monsterRoom.setTaskedNeighbors(Vector(                             "north" -> artifactRoom                                                      ))
  artifactRoom.setNeighbors(Vector(                                                                                                               ))
  artifactRoom.addItem(artifact)


  /** The number of turns that have passed since the start of the game. */
  var turnCount = 0
  /** The maximum number of turns that this adventure game allows before time runs out. */
  val timeLimit = 100
  /** The character that the player controls in the game. */
  val player = new Player(entrance)

  /** Determines if the adventure is complete, that is, if the player has won. */
  def isComplete = this.player.location == this.destination && (this.player.has("artifact"))

  /** Determines whether the player has won, lost, or quit, thereby ending the game. */
  def isOver = this.isComplete || this.player.hasDied || this.player.hasQuit || this.turnCount == this.timeLimit

  /** Returns a message that is to be displayed to the player at the beginning of the game. */
  def welcomeMessage = "You have reached the dungeon where the long lost artifact is rumoured to be.\nThe artifact will most certainly be well guarded. You will face many dangers down there.\nBut if you manage to retrieve the artifact,you will be the most powerful of all mankind!"


  /** Returns a message that is to be displayed to the player at the end of the game. The message
    * will be different depending on whether or not the player has completed their quest. */
  def goodbyeMessage = {
    if (this.isComplete)
      "Who would have thought?! Both you and the artifact have made it out of the dungeon. Well done!"
    else if (this.turnCount == this.timeLimit)
      "Oh no! Time's up.\nAs you question your choices in life, the dungeon caves in, rocks fall and everyone dies.\nGame over!"
    else if(this.player.hasDied)
      "You fought bravely, but will join those who never see sunlight again"
    else
      "Quitter!"
  }


  /** Added code for the challenge in timerRoom. Otherwise unchanged.
    * Plays a turn by executing the given in-game command, such as "go west". Returns a textual
    * report of what happened, or an error message if the command was unknown. In the latter
    * case, no turns elapse. */
  def playTurn(command: String):String = {
    val loc = this.player.location
    val action = new Action(command)
    val outcomeReport = action.execute(this.player)
    if (outcomeReport.isDefined) {
      this.turnCount += 1
    }
    if (-1 < loc.task.turnsLeft && loc.task.turnsLeft <4 && loc == timerRoom  && command != "push") {
      loc.task.turnsLeft -= 1
      if (loc.task.turnsLeft < 1) {
        loc.completeTask()
        "The ground finishes shaking. You survived without heart-failure. The doors open and you may proceed!"
      } else s"The ground shakes again. ${loc.task.turnsLeft.toString} are still lit. The end creeps closer unless you come up with something!"
      }
    else outcomeReport.getOrElse("Unknown command: \"" + command + "\".").toString
  }

}

