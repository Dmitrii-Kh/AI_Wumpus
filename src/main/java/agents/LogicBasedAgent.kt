package agents

import wumpus.Agent
import wumpus.Environment
import wumpus.Player
import java.util.*
import kotlin.collections.HashMap


data class LogicBasedAgent(var width: Int, var height: Int) : Agent{
    private var w: Int = width
    private var h: Int = height

    private val debug = false
    private var isBREEZE = Array(w) {BooleanArray(h)}
    private var isVisited = Array(w) {BooleanArray(h)}
    private var isSTENCH = Array(w) {BooleanArray(h)}
    private val timesVisited = Array(w) { Array<Int>(h) {0} }
    private var isBUMP = Array(w) {Array<Player.Direction>(h){Player.Direction.E} }
    private var isSCREAM = false

    private val board: HashMap<Point, ArrayList<Environment.Perception>> = HashMap()
    private val nextActions = LinkedList<Environment.Action>()

    // TODO create one hashmap of Perception

//    private val isBUMP: HashMap<Int, ArrayList<Player.Direction>> = HashMap()
//    private val isVisited: HashMap<Int, ArrayList<Boolean>> = HashMap()
//    private val isBREEZE: HashMap<Int, ArrayList<Boolean>> = HashMap()
//    private val isSTENCH: HashMap<Int, ArrayList<Boolean>> = HashMap()

//    private val timesVisited: HashMap<Int, ArrayList<Int>> = HashMap()

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


    override fun getAction(player: Player): Environment.Action? {
        if (nextActions.size > 0) {
            return nextActions.poll()
        }
        val x = player.x
        val y = player.y
        tell(player)
        if (player.hasGlitter()) {
            return Environment.Action.GRAB
        }
        val neighbours = getNeighbors(x, y)
        val neibs: ArrayList<Point> = ArrayList<Point>()
        for (n in neighbours) {
            if (!isVisited[n[0]][n[1]] && isNotWumpus(n[0], n[1]) && isNotPit(n[0], n[1])) {
                val actions = getActionsTo(player, n)
                nextActions.addAll(actions)
                return nextActions.poll()
            } else if (player.hasArrows() && isWumpus(n[0], n[1])) {
                val actions = getActionsToShoot(player, n)
                nextActions.addAll(actions)
                return nextActions.poll()
            }
        }
        for (n in neighbours) {
            var value = 0
            if (isVisited[n[0]][n[1]]) {
                // neibs.add(new MyPoint(n[0], n[1], timesVisited[n[0]][n[1]] == 3 ? 1 : 5));
                value = if(timesVisited[n[0]][n[1]] == 3) 1 else 5
                neibs.add(Point(n[0], n[1], value))
            } else if (!isVisited[n[0]][n[1]] && (isNotWumpus(n[0], n[1]) || isNotPit(n[0], n[1]))) {
                neibs.add(Point(n[0], n[1], value))
            }
        }
        Collections.sort(neibs, Collections.reverseOrder<Any>())
        val next = intArrayOf(neibs[0].x, neibs[0].y)
        val actions = getActionsTo(player, next)
        nextActions.addAll(actions)
        return nextActions.poll()
    }

    // add info about tile to 'knowledge base'
    private fun tell(player: Player) {
        val x = player.x
        val y = player.y
        timesVisited[x][y] += 1
        isVisited[x][y] = true
        if (player.hasBreeze()) {
            isBREEZE[x][y] = true
        }
        if (player.hasStench()) {
            isSTENCH[x][y] = true
        }
        if (player.hasBump()) {
            isBUMP[x][y] = player.direction
        }
        if (player.hasScream()) {
            isSCREAM = true
        }
    }

    private fun isWumpus(x: Int, y: Int): Boolean {
        if (isSCREAM || isVisited[x][y]) {
            return false
        }
        val west = intArrayOf(x - 1, y)
        val north = intArrayOf(x, y + 1)
        val east = intArrayOf(x + 1, y)
        val south = intArrayOf(x, y - 1)
        if (isValid(south[0], south[1]) && isVisited[south[0]][south[1]] && isSTENCH[south[0]][south[1]]) {
            if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]] && isVisited[x + 1][y - 1]) {
                return true
            }
            if (isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]] && isVisited[x - 1][y - 1]) {
                return true
            }
            if (isValid(north[0], north[1]) && isVisited[north[0]][north[1]] && isSTENCH[north[0]][north[1]]) {
                return true
            }
        }
        if (isValid(north[0], north[1]) && isVisited[north[0]][north[1]] && isSTENCH[north[0]][north[1]]) {
            if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]] && isVisited[x + 1][y + 1]) {
                return true
            }
            if (isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]] && isVisited[x - 1][y + 1]) {
                return true
            }
        }
        if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]] &&
                isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]]) {
            return true
        }
        if (isValid(south[0], south[1]) && isVisited[south[0]][south[1]] && isSTENCH[south[0]][south[1]]
                && (!isValid(south[0], south[1] - 1) || isVisited[south[0]][south[1] - 1])) {
            if (isValid(x + 1, y - 1) && isVisited[x + 1][y - 1] && isBUMP[south[0]][south[1]] == Player.Direction.W
                    || isValid(x - 1, y - 1) && isVisited[x - 1][y - 1] && isBUMP[south[0]][south[1]] == Player.Direction.E) {
                return true
            }
        }
        if (isValid(north[0], north[1]) && isVisited[north[0]][north[1]] && isSTENCH[north[0]][north[1]]
                && (!isValid(north[0], north[1] + 1) || isVisited[north[0]][north[1] + 1])) {
            if (isValid(x + 1, y + 1) && isVisited[x + 1][y + 1] && isBUMP[north[0]][north[1]] == Player.Direction.W
                    || isValid(x - 1, y + 1) && isVisited[x - 1][y + 1] && isBUMP[north[0]][north[1]] == Player.Direction.E) {
                return true
            }
        }
        if (isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]]
                && (!isValid(west[0] - 1, west[1]) || isVisited[west[0] - 1][west[1]])) {
            if (isValid(x - 1, y - 1) && isVisited[x - 1][y - 1] && isBUMP[west[0]][west[1]] == Player.Direction.N
                    || isValid(x - 1, y + 1) && isVisited[x - 1][y + 1] && isBUMP[west[0]][west[1]] == Player.Direction.S) {
                return true
            }
        }
        if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]]
                && (!isValid(east[0] + 1, east[1]) || isVisited[east[0] + 1][east[1]])) {
            if (isValid(x + 1, y - 1) && isVisited[x + 1][y - 1] && isBUMP[east[0]][east[1]] == Player.Direction.N
                    || isValid(x + 1, y + 1) && isVisited[x + 1][y + 1] && isBUMP[east[0]][east[1]] == Player.Direction.S) {
                return true
            }
        }
        return false
    }

    private fun isNotWumpus(x: Int, y: Int): Boolean {
        if (isSCREAM || isVisited[x][y]) {
            return true
        }

        //neighbours are numbered from west to south
        val neighbours = Array(4) { IntArray(2) }
        neighbours[0] = intArrayOf(x - 1, y)
        neighbours[1] = intArrayOf(x, y + 1)
        neighbours[2] = intArrayOf(x + 1, y)
        neighbours[3] = intArrayOf(x, y - 1)
        for (n in neighbours) {
            if (isValid(n[0], n[1]) && isVisited[n[0]][n[1]] && !isSTENCH[n[0]][n[1]]) {
                return true
            }
        }
        return false
    }

    private fun isNotPit(x: Int, y: Int): Boolean {
        if (isVisited[x][y]) {
            return true
        }

        //neighbours are numbered from west to south
        val neighbours = Array(4) { IntArray(2) }
        neighbours[0] = intArrayOf(x - 1, y)
        neighbours[1] = intArrayOf(x, y + 1)
        neighbours[2] = intArrayOf(x + 1, y)
        neighbours[3] = intArrayOf(x, y - 1)
        for (n in neighbours) {
            if (isValid(n[0], n[1]) && isVisited[n[0]][n[1]] && !isBREEZE[n[0]][n[1]]) {
                return true
            }
        }
        return false
    }

    private fun isValid(x: Int, y: Int): Boolean {
        return x < w && x > -1 && y > -1 && y < h
    }

    /**
     * Gets the adjacent tiles of the given coordinates.
     *
     * @param x The tile X coordinate
     * @param y The tile Y coordinate
     * @return An array of 2D coordinates
     */
    private fun getNeighbors(x: Int, y: Int): Array<IntArray> {
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
        var branch = 0
        val nodes = Array(nodesMap.size) { IntArray(2) }
        for (direction in nodesMap.keys) {
            when (direction) {
                Player.Direction.N -> nodes[branch] = intArrayOf(x, north)
                Player.Direction.S -> nodes[branch] = intArrayOf(x, south)
                Player.Direction.E -> nodes[branch] = intArrayOf(east, y)
                Player.Direction.W -> nodes[branch] = intArrayOf(west, y)
            }
            branch++
        }
        return nodes
    }

    /**
     * Returns the amount of turns player need to take to get into given position.
     *
     * @param player The player's instance
     * @param to     The destination tile
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
        val lenProduct = Math.hypot(from[0].toDouble(), from[1].toDouble()) * Math.hypot(dest[0].toDouble(), dest[1].toDouble())
        var theta = Math.acos(dotProduct / lenProduct)
        // Inverts when facing backwards
        if (player.direction == Player.Direction.N && getDirection(dest) == Player.Direction.E || player.direction == Player.Direction.E && getDirection(dest) == Player.Direction.S || player.direction == Player.Direction.S && getDirection(dest) == Player.Direction.W || player.direction == Player.Direction.W && getDirection(dest) == Player.Direction.N) {
            theta *= -1.0
        }
        // Count how many turns
        return (theta / (Math.PI / 2)).toInt()
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     *
     * @param player The player's instance
     * @param to     The destination tile coordinates
     * @return An array of actions
     */
    private fun getActionsTo(player: Player, to: IntArray): ArrayList<Environment.Action> {
        val actions = ArrayList<Environment.Action>()
        val turns = getTurns(player, to)
        for (i in 0 until Math.abs(turns)) {
            if (turns < 0) actions.add(Environment.Action.TURN_RIGHT)
            if (turns > 0) actions.add(Environment.Action.TURN_LEFT)
        }
        // Go to the block
        actions.add(Environment.Action.GO_FORWARD)
        return actions
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     *
     * @param player The player's instance
     * @param to     The destination tile coordinates
     * @return An array of actions
     */
    private fun getActionsToShoot(player: Player, to: IntArray): ArrayList<Environment.Action> {
        val actions = ArrayList<Environment.Action>()
        val turns = getTurns(player, to)
        for (i in 0 until Math.abs(turns)) {
            if (turns < 0) actions.add(Environment.Action.TURN_RIGHT)
            if (turns > 0) actions.add(Environment.Action.TURN_LEFT)
        }
        // Go to the block
        actions.add(Environment.Action.SHOOT_ARROW)
        return actions
    }

    /**
     * Returns the direction based on the vector coordinates
     *
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