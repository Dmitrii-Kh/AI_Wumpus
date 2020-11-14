package agents

import wumpus.Agent
import wumpus.Environment
import wumpus.Player
import java.util.*
import kotlin.collections.HashMap

data class LogicAgent(var width: Int, var height: Int) : Agent{
    private var w: Int = width
    private var h: Int = height

    private val debug = true

    private val isBUMP: HashMap<Int, ArrayList<Player.Direction>> = HashMap()
    private val isVisited: HashMap<Int, ArrayList<Boolean>> = HashMap()
    private val isBREEZE: HashMap<Int, ArrayList<Boolean>> = HashMap()
    private val isSTENCH: HashMap<Int, ArrayList<Boolean>> = HashMap()
    private val isSCREAM = false
    private val timesVisited: HashMap<Int, ArrayList<Int>> = HashMap()
    private val nextActions = LinkedList<Environment.Action>()

    override fun beforeAction(player: Player?) {
        if (debug) {
            println(player?.render())
            println(player?.debug())
        }
    }

    override fun afterAction(player: Player?) {
        if (debug) {
            // Players Last action
            println(player!!.lastAction)
            // Show a very happy message
            if (player.isDead) {
                println("GAME OVER!")
            }
            // Turn on step-by-step
            Environment.trace()
        }
    }

    override fun getAction(player: Player?): Environment.Action {
        TODO("Not yet implemented")
    }

}