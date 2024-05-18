package generics

import configuration.*
import neuronalNet.*
import utils.*

typealias Fitness = Int
typealias Index = Int
typealias Size = Int
typealias Weight = Double
typealias Weights = DoubleArray
typealias ReadWeight = (Index) -> Weight
typealias ReadGenome = (Index) -> Genome

/**
 * Genome stores weights as an array internally, but external access is read-only by index.
 * The only way the weights could be changed during the Genome's lifetime is if the Genome's
 * creator altered the array's contents (that it provided) after the fact.
 */
class Genome(private val ws: Weights, var fitness: Fitness = 0, var isElite: Boolean = false) : Comparable<Genome> {
    fun weight(idx: Index) = ws[idx]
    override fun compareTo(other: Genome) = fitness.compareTo(other.fitness)
}

@Suppress("SpellCheckingInspection")
class GeneticAlgorithm {
    companion object {
        fun Int.isEven() = this % 2 == 0

        @Suppress("NOTHING_TO_INLINE")
        inline fun Size.idx(): Index = this - 1

        // all the parameters below are determined lazily, so that they can be read into
        // Parameters before the GenAlg instance is created.
        @JvmStatic
        val genomeCount: Size by lazy { Parameters.iNumSweepers.also { require(it > 0 && it.isEven()) { "population size: $it, must be positive amd even" } } }

        @JvmStatic
        val desiredElites: Size by lazy { Parameters.iNumElite.also { require(it > 0) { "elites: $it, must be positive" } } }

        private val copiesPerElite: Size by lazy { Parameters.iNumCopiesElite.also { require(it > 0) { "copies per elite: $it, must be positive" } } }
        private val dMaxPerturbation by lazy { Parameters.dMaxPerturbation }
        private val mutationRate by lazy { Parameters.dMutationRate }
        private val crossoverRate by lazy { Parameters.dCrossoverRate }

        private fun Weight.allowForPossibleMutation() =
            if (rand.randomFloat() >= mutationRate) this else this + (rand.randomClamped() * dMaxPerturbation)

        private val chromosomeLength: Size by lazy { NeuralNet.numberOfWeights }

        private fun crossover(at: Index?, count: Size, fxA: ReadWeight, fxB: ReadWeight): Pair<Weights, Weights> {
            // turn the cross-over point into a simple question: is this point before the cross-over?
            val beforeCrossover: (Index) -> Boolean =
                at?.let { cp -> { idx -> idx < cp } }
                // of course, if there's no cross-over point, then we're always 'before' it.
                    ?: { true }

            // NOTE: as well as making it possible to initialize an array while creating it
            fun possibleSwitchAndMutation(forChildA: Boolean): (Index) -> Weight {
                // determine the before/after weights by child: Child A starts with Parent A, Child B with Parent B
                val (fxBefore, fxAfter) = if (forChildA) fxA to fxB else fxB to fxA
                // now we can return a function that reads the correct weight by index alone ...
                return { idx: Index ->
                    (if (beforeCrossover(idx)) fxBefore(idx) else fxAfter(idx))
                        // .. but each weight has the chance to mutate before all become 'fixed'
                        .allowForPossibleMutation()
                }
            }

            val childA = Weights(count, possibleSwitchAndMutation(true))
            val childB = Weights(count, possibleSwitchAndMutation(false))
            return childA to childB
        }

        private fun examinePopulation(fxGenome: ReadGenome): Scuplt {
            // Two rare cases exist; we'd like to handle them in much the same way as if they didn't.
            if (fxGenome(0).fitness == 0) {
                // 1) if our bestFitness is zero, we're in big trouble: a roulette weighted by fitness
                //    doesn't work at all, and if we wanted elites, there aren't any.
                //    So, we'll consider the whole population equally likely for pairings.
                return Scuplt(0, 0, genomeCount.idx()) { carry, _ -> carry + 1 }
            }

            // Especially in early generations, there's the chance of having 1+ genomes in the population
            // with a fitness of zero, which impacts our binary search of accumulated fitness, so we need
            // to find the number of fit genomes to bound the roulette wheel search
            val fit: Size = (1..genomeCount).findLast { fxGenome(it.idx()).fitness > 0 } ?: 0

            if (fit >= desiredElites) {
                // the usual case, weighting is fitness as-is, and zero-fitness genomes are left out of pairings
                return Scuplt(desiredElites, copiesPerElite, fit.idx()) { c, g -> c + g.fitness }
            }

            // 2) almost as bad, there aren't enough fit genomes as desired elites, so again we must
            //    adjust our population's likelihood for pairings, though still favoring the elites.
            //    also, copies * (actual) elites may no longer be even.
            val atleast = desiredElites * copiesPerElite
            // if we can simply make more copies, the new number of copies should be in this range
            val (copies, w) = (atleast / fit..atleast)
                // but the total still needs to be large enough (but not too large) and an even number
                .find { (fit * it).let { total -> total in atleast..atleast * 2 && total.isEven() } }
                // and if we got an answer, use it, and weight the fit genomes more than the unfit
                // otherwise, go with the original number, but weight the fit genomes even moreso
                ?.let { it to 5 } ?: (copiesPerElite to 10)
            // each genome has some chance to be chosen for pairings
            return Scuplt(fit, copies, genomeCount.idx()) { c, g -> c + g.fitness * w + 1 }
        }

        private fun nextPopulationBy(
            fxGenome: ReadGenome,
            scupltor: Scuplt,
            chromosomeLength: Size,
            runRoulette: () -> Index
        ) = sequence {
            repeat(scupltor.elite) { idxElite ->
                repeat(scupltor.copies) {
                    yield(fxGenome(idxElite).also { it.fitness = 0; it.isElite = true })
                }
            }

            // So, I'm not really fond of the cross-over determination as historically implemented.
            // I'm not changing it right now, to keep the expected population fitness growth as
            // previously seen, but I want to note that as-written, the cross-over rate experienced
            // is always going to be smaller than the rate specified.
            //
            // 1) As the determination of parentB doesn't exclude parentA, when they are the same,
            //    this *also* means no cross-over.
            // 2) The cross-over point includes the possibility of index 0 which means the children
            //    are each still entirely from one parent, effectively the same as no cross-over.
            //
            // However, even with no cross-over, mutation may mean the children aren't identical to
            // the parents, nor to each other.  Roll enough dice and the chances of not seeing a '1'
            // become increasingly scant.

            repeat((genomeCount - scupltor.elite * scupltor.copies) / 2) {
                val parentA = fxGenome(runRoulette())
                val parentB = fxGenome(runRoulette())
                // determine if there should be a cross-over, and at what point
                val crossOverPoint =
                    if (parentA !== parentB && rand.randomFloat() <= crossoverRate)
                        rand.randomInt(0, chromosomeLength - 1)
                    else null

                val (weightsChildA, weightsChildB) =
                    crossover(crossOverPoint, chromosomeLength, parentA::weight, parentB::weight)
                yield(Genome(weightsChildA))
                yield(Genome(weightsChildB))
            }
        }
    }

    init {
        val alwaysThisManyFromOldPopulation = desiredElites * copiesPerElite
        require(alwaysThisManyFromOldPopulation.isEven()) { "(elites: $desiredElites) times (copies per elite: $copiesPerElite) must be an even number" }
        require(alwaysThisManyFromOldPopulation <= genomeCount) { "(elites: $desiredElites) times (copies per elite: $copiesPerElite) must not be bigger than the population size: $genomeCount" }
    }

    private var population = Array(genomeCount) { Genome(Weights(chromosomeLength) { rand.randomClamped() }) }

    fun genome(idx: Index) = population[idx]

    val chromos get() = population.toList()

    var bestFitness = 0
    var medianFitness = 0

    private class Scuplt(
        val elite: Size,
        val copies: Size,
        val lastFitGenome: Index,
        val fxWheel: (Fitness, Genome) -> Fitness
    )

    /** Determines stats for old generation, and puts new one in place. */
    fun runEpoch(): List<Genome> {
        // This will sort the existing population from strongest to weakest.
        // genome's compareTo looks only at its fitness
        population.sortDescending()
        bestFitness = population.first().fitness

        val scupltor = examinePopulation(::genome)
        medianFitness = population[scupltor.lastFitGenome / 2].fitness

        // we don't use runningFold to construct the weighted roulette wheel, because the result would be
        // one element larger than the original, and we want a result of the same size
        val weightedWheel = population.carriedFold(0, scupltor.fxWheel)

        fun runRoulette() =
            rand.randomInt(0, weightedWheel.last())
                .let { weightedWheel.binarySearch(it, toIndex = scupltor.lastFitGenome) }
                .let { if (it >= 0) it else -it - 1 }

        val newPopulation = nextPopulationBy(::genome, scupltor, chromosomeLength, ::runRoulette).toList()
        population = newPopulation.toTypedArray()
        return newPopulation
    }
}

/** Iterable.runningFold but the initial carry doesn't become the first element, so the result is the same size. */
fun <T, R> Iterable<T>.carriedFold(carry: R, mapping: (R, T) -> R) = iterator {
    var carried = carry
    for (t in this@carriedFold) {
        carried = mapping(carried, t).also { yield(it) }
    }
}

inline fun <T, reified R> Array<out T>.carriedFold(carry: R, noinline mapping: (acc: R, T) -> R) =
    if (isEmpty()) emptyArray()
    else asIterable().carriedFold(carry, mapping).iterator().let { seq -> Array(size) { seq.next() } }
