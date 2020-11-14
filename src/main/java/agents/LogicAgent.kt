package agents

import wumpus.Agent
import wumpus.Environment
import wumpus.Player
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.hypot


data class LogicAgent(var width: Int, var height: Int) : Agent{
    private var w: Int = width
    private var h: Int = height

    private val debug = true

    // TODO create one hashmap of Perception


    private val board: HashMap<Point, ArrayList<Environment.Perception>> = HashMap()
//    private val isBUMP: HashMap<Int, ArrayList<Player.Direction>> = HashMap()
//    private val isVisited: HashMap<Int, ArrayList<Boolean>> = HashMap()
//    private val isBREEZE: HashMap<Int, ArrayList<Boolean>> = HashMap()
//    private val isSTENCH: HashMap<Int, ArrayList<Boolean>> = HashMap()

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

    // TODO implement
    override fun getAction(player: Player?): Environment.Action {
        TODO("Not yet implemented")
    }

    private fun isValid(x: Int, y: Int): Boolean {
        return x < w && x > -1 && y > -1 && y < h
    }


    //TODO PEREDELAT' POD HASHMAP
    private fun getNeighbors(x: Int, y: Int): Array<IntArray>? {
        val nodesMap = HashMap<Player.Direction, Int>()

        // Calculate the next block
        val north = y - 1
        val south = y + 1
        val east = x + 1
        val west = x - 1

        // Check if branch is into bounds
        if (north >= 0) nodesMap[Player.Direction.N] = north
        if (south < h) nodesMap[Player.Direction.S] = south
        if (east < w) nodesMap[Player.Direction.E] = east
        if (west >= 0) nodesMap[Player.Direction.W] = west

        // Build the branches array
        val nodes = Array(nodesMap.size) { IntArray(2) }
        for ((branch, direction) in nodesMap.keys.withIndex()) {
            when (direction) {
                Player.Direction.N -> nodes[branch] = intArrayOf(x, north)
                Player.Direction.S -> nodes[branch] = intArrayOf(x, south)
                Player.Direction.E -> nodes[branch] = intArrayOf(east, y)
                Player.Direction.W -> nodes[branch] = intArrayOf(west, y)
            }
        }
        return nodes
    }

    /**
     * Returns the amount of turns player need to take to get into given position.
     * @param player The player's instance
     * @param to The destination tile
     * @return The number of turns
     */
    private fun getTurns(player: Player, to: IntArray): Int {
        // The current vector
        val from = intArrayOf(1, 0)
        when (player.direction) {
            Player.Direction.N -> {
                from[0] = 0
                from[1] = 1
            }
            Player.Direction.S -> {
                from[0] = 0
                from[1] = -1
            }
            Player.Direction.W -> {
                from[0] = -1
                from[1] = 0
            }
        }
        // The destination vector
        val dest = intArrayOf(to[0] - player.x, player.y - to[1])
        // The angle between the two vectors
        val dotProduct = from[0] * dest[0] + from[1] * dest[1].toDouble()
        val lenProduct = hypot(from[0].toDouble(), from[1].toDouble()) * hypot(dest[0].toDouble(), dest[1].toDouble())
        var theta = acos(dotProduct / lenProduct)
        // Inverts when facing backwards
        if (player.direction == Player.Direction.N && getDirection(dest) == Player.Direction.E || player.direction == Player.Direction.E && getDirection(dest) == Player.Direction.S || player.direction == Player.Direction.S && getDirection(dest) == Player.Direction.W || player.direction == Player.Direction.W && getDirection(dest) == Player.Direction.N) {
            theta *= -1.0
        }
        // Count how many turns
        return (theta / (Math.PI / 2)).toInt()
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     * @param player The player's instance
     * @param to The destination tile coordinates
     * @return An array of actions
     */
    private fun getActionsTo(player: Player, to: IntArray): ArrayList<Environment.Action>? {
        val actions = ArrayList<Environment.Action>()
        val turns = getTurns(player, to)
        for (i in 0 until abs(turns)) {
            if (turns < 0) actions.add(Environment.Action.TURN_RIGHT)
            if (turns > 0) actions.add(Environment.Action.TURN_LEFT)
        }
        // Go to the block
        actions.add(Environment.Action.GO_FORWARD)
        return actions
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     * @param player The player's instance
     * @param to The destination tile coordinates
     * @return An array of actions
     */
    private fun getActionsToShoot(player: Player, to: IntArray): ArrayList<Environment.Action>? {
        val actions = ArrayList<Environment.Action>()
        val turns = getTurns(player, to)
        for (i in 0 until abs(turns)) {
            if (turns < 0) actions.add(Environment.Action.TURN_RIGHT)
            if (turns > 0) actions.add(Environment.Action.TURN_LEFT)
        }
        // Go to the block
        actions.add(Environment.Action.SHOOT_ARROW)
        return actions
    }

    /**
     * Returns the direction based on the vector coordinates
     * @param coords The 2D coordinates
     * @return The direction
     */
    private fun getDirection(coords: IntArray): Player.Direction {
        if (coords[0] == +0 && coords[1] == +1) return Player.Direction.N
        if (coords[0] == +1 && coords[1] == +0) return Player.Direction.E
        if (coords[0] == +0 && coords[1] == -1) return Player.Direction.S
        return if (coords[0] == -1 && coords[1] == +0) Player.Direction.W else Player.Direction.E
    }


}