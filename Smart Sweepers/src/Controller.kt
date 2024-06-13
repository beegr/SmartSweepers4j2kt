import GeneticAlgorithm.Companion.copiesPerElite
import GeneticAlgorithm.Companion.desiredElites
import GeneticAlgorithm.Companion.genomeCount
import GeneticAlgorithm.Companion.idx
import rand.randomFloat

typealias DrawLine = (x1: Double, y1: Double, x2: Double, y2: Double) -> Unit
typealias DrawText = (s: String, x: Int, y: Int) -> Unit
typealias ChangePen = () -> Unit
typealias RenderTransform = (DrawLine, Matrix) -> Unit
typealias Generation = Int
typealias PeakPoint = Pair<Generation, Fitness>

val PeakPoint.gen: Generation
    inline get() = this.first
val PeakPoint.fit: Fitness
    inline get() = this.second

class Controller {
    lateinit var line: DrawLine
    lateinit var text: DrawText
    lateinit var oldPen: ChangePen
    lateinit var redPen: ChangePen
    lateinit var greenPen: ChangePen
    lateinit var bluePen: ChangePen
    lateinit var repaint: () -> Unit

    private val genAlg = GeneticAlgorithm()
    private val sweepers =
        List(genomeCount) { si -> Minesweeper().also { it.putWeights { pi -> genAlg[si].weight(pi) } } }

    private val xClient: Size = Parameters.WindowWidth
    private val yClient: Size = Parameters.WindowHeight

    private val numMines: Int = Parameters.iNumMines
    private val mineLocations = MutableList(numMines) { Point(randomFloat() * xClient, randomFloat() * yClient) }

    companion object {
        private val sweeperScale by lazy {
            Parameters.iSweeperScale.toDouble()
                .also { require(it > 0.0) { "sweeper scale: $it, must be positive" } }
        }

        private val mineScale by lazy {
            Parameters.dMineScale
                .also { require(it > 0.0) { "mine scale: $it, must be positive" } }
        }

        private val closeEnough by lazy {
            sweeperScale + mineScale
        }

        private fun createOutlineRenderer(points: List<Point>, lines: List<Pair<Index, Index>>): RenderTransform {
            val pointsUsed = lines.flatMap { (a, b) -> listOf(a, b) }.distinct()
            val pointsAvailable = 0..points.size.idx()
            require(pointsUsed.all { it in pointsAvailable }) { "some lines use points not included" }

            return { draw, matrix ->
                val xPoints = matrix.transformPoints(points)
                lines.forEach { (a, b) -> draw(xPoints[a].x, xPoints[a].y, xPoints[b].x, xPoints[b].y) }
            }
        }

        val drawSweeper: RenderTransform by lazy {
            createOutlineRenderer(
                Matrix.startWith.scaling(sweeperScale, sweeperScale).transformPoints(
                    listOf(
                        Point(-1.0, -1.0), // right track
                        Point(-1.0, 1.0),
                        Point(-0.5, 1.0),
                        Point(-0.5, -1.0),

                        Point(0.5, -1.0), // left track
                        Point(1.0, -1.0),
                        Point(1.0, 1.0),
                        Point(0.5, 1.0),

                        Point(-0.5, -0.5), // rear
                        Point(0.5, -0.5),

                        Point(-0.5, 0.5), // front
                        Point(-0.25, 0.5),
                        Point(-0.25, 1.75),
                        Point(0.25, 1.75),
                        Point(0.25, 0.5),
                        Point(0.5, 0.5)
                    )
                ),
                listOf(
                    0 to 1, 1 to 2, 2 to 3, 3 to 0,
                    4 to 5, 5 to 6, 6 to 7, 7 to 4,
                    8 to 9,
                    10 to 11, 11 to 12, 12 to 13, 13 to 14, 15 to 10
                )
            )
        }

        val drawMine: RenderTransform by lazy {
            createOutlineRenderer(
                Matrix.startWith.scaling(mineScale, mineScale).transformPoints(
                    listOf(Point(-1.0, -1.0), Point(-1.0, 1.0), Point(1.0, 1.0), Point(1.0, -1.0))
                ),
                listOf(0 to 1, 1 to 2, 2 to 3, 3 to 0)
            )
        }
    }

    private val medianFitness = mutableListOf<Fitness>()
    private val bestFitness = mutableListOf<Fitness>()
    private var peak: PeakPoint = -1 to 0
    private val peakFitness: MutableList<PeakPoint> = mutableListOf(-1 to 0)

    var fastRender = false
    fun fastRenderToggle() {
        fastRender = !fastRender
    }

    private var ticks: Int = 0
    private var generations: Int = 0
    private var thePopulation = genAlg.chromes

    private fun worldTransformMatrixFor(pos: Point) =
        Matrix.startWith.translation(pos.x, pos.y)

    fun update() {
        if (ticks++ < Parameters.iNumTicks) {
            sweepers.forEachIndexed { i, currentSweeper ->
                currentSweeper.update(mineLocations)

                val grabHit = currentSweeper.checkForMine(mineLocations, closeEnough)

                if (grabHit >= 0) {
                    currentSweeper.incrementFitness()
                    mineLocations[grabHit] = Point(randomFloat() * xClient, randomFloat() * yClient)
                }

                thePopulation[i].fitness = currentSweeper.fitness
            }
        } else {
            ticks = 0

            thePopulation = genAlg.runEpoch()
            medianFitness.add(genAlg.medianFitness)
            bestFitness.add(genAlg.bestFitness)
            if (genAlg.bestFitness > peak.fit) {
                peakFitness.add(generations to peak.fit)
                peak = generations to genAlg.bestFitness
                peakFitness.add(peak)
            }

            sweepers.forEachIndexed { i, sweeper ->
                sweeper.putWeights { idx: Int -> thePopulation[i].weight(idx) }
                sweeper.reset()
            }
            generations++
            repaint()
        }
    }

    private fun createLineGraph(): (Iterable<Pair<Generation, Fitness>>) -> Unit {
        // points in graph are x,y of (generation, fitness)
        // to fit line-graph within screen ...
        val hScale = xClient / (generations + 1.0)
        val vScale = (yClient - 80.0) / (peak.fit + 1.0)
        val graphWorld = Matrix
            // ... but remember, the screen's y increases downward ...
            .startWith.scaling(hScale, -vScale)
            // ... with the origin at the top
            .thenTranslate(0.0, yClient.toDouble())

        return { info ->
            info.map { (g, f) -> g.toDouble() to f.toDouble() }
                .let { points -> graphWorld.transformPoints(points) }
                .windowed(2)
                .forEach { line(it[0].x, it[0].y, it[1].x, it[1].y) }
        }
    }

    fun render() {
        if (!fastRender) {
            // mines are green
            greenPen()
            mineLocations.forEach { drawMine(line, worldTransformMatrixFor(it)) }

            // sweepers are ordered strongest to weakest, so elites are first, and red
            redPen()
            // (there are no elites in generation zero, though.)
            val doneWithElites = if (generations > 0) desiredElites * copiesPerElite else 0
            sweepers.forEachIndexed { i, sweeper ->
                // when finished with the elites, switch back to usual color
                if (i == doneWithElites) oldPen()

                drawSweeper(line, sweeper.worldTransformMatrix())
            }
        } else {
            // draw lines only if there's history
            if (bestFitness.isNotEmpty()) {
                // to fit line-graph within screen
                val lineGraph = createLineGraph()

                greenPen()
                lineGraph(peakFitness.plusElement(generations to peak.fit))

                // best fitness drawn in red (same color as elites)
                redPen()
                lineGraph(bestFitness.mapIndexed { idx, f -> idx to f })

                // average fitness in blue
                bluePen()
                lineGraph(medianFitness.mapIndexed { idx, f -> idx to f })

                oldPen()
            }
            text("Peak Fitness: ${peak.fit} [Gen: ${peak.gen}]", 5, 40)
            text("Best Fitness: ${genAlg.bestFitness}", 5, 60)
            text("Median Fitness: ${genAlg.medianFitness}", 5, 80)
        }
        // ensuring usual color restored in any case.
        oldPen()
        text("Generation: $generations", 5, 20)
    }
}