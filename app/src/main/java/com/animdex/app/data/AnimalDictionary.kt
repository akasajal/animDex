package com.animdex.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector

enum class AnimalGroup(val displayName: String, val isAnimal: Boolean = true) {
    MAMMAL("Mammal", true),
    BIRD("Bird", true),
    REPTILE("Reptile", true),
    AMPHIBIAN("Amphibian", true),
    FISH("Fish", true),
    INVERTEBRATE("Invertebrate", true),
    NOT_ANIMAL("Inanimate Object", false);

    val icon: ImageVector
        get() = when (this) {
            MAMMAL -> Icons.Default.Pets
            BIRD -> Icons.Default.Flight
            REPTILE -> Icons.Default.Terrain
            AMPHIBIAN -> Icons.Default.WaterDrop
            FISH -> Icons.Default.Waves
            INVERTEBRATE -> Icons.Default.BugReport
            NOT_ANIMAL -> Icons.Default.Category
        }

    val emoji: String
        get() = ""
}

data class ParsedSpecies(
    val displayName: String,
    val scientificName: String
)

data class AnimalInfo(
    val displayName: String,
    val scientificName: String,
    val group: AnimalGroup,
    val isAnimal: Boolean,
    val funFact: String
)

object AnimalDictionary {

    fun parseSpeciesLabel(rawLabel: String): ParsedSpecies {
        val trimmed = rawLabel.trim()
        val parenIndex = trimmed.indexOf('(')
        val closeParenIndex = trimmed.lastIndexOf(')')

        return if (parenIndex != -1 && closeParenIndex > parenIndex) {
            val scientific = trimmed.substring(0, parenIndex).trim()
            val common = trimmed.substring(parenIndex + 1, closeParenIndex).trim()
            ParsedSpecies(
                displayName = common.ifEmpty { scientific },
                scientificName = scientific
            )
        } else {
            val clean = trimmed.lowercase().replace('_', ' ').trim()
            val formatted = clean.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            ParsedSpecies(
                displayName = formatted,
                scientificName = ""
            )
        }
    }

    fun getInfoForSpecializedBird(rawLabel: String): AnimalInfo {
        val parsed = parseSpeciesLabel(rawLabel)
        val fact = getFact(parsed.displayName.lowercase(), AnimalGroup.BIRD)
        return AnimalInfo(
            displayName = parsed.displayName,
            scientificName = parsed.scientificName,
            group = AnimalGroup.BIRD,
            isAnimal = true,
            funFact = fact
        )
    }

    fun getInfoForSpecializedInsect(rawLabel: String): AnimalInfo {
        val parsed = parseSpeciesLabel(rawLabel)
        val fact = getFact(parsed.displayName.lowercase(), AnimalGroup.INVERTEBRATE)
        return AnimalInfo(
            displayName = parsed.displayName,
            scientificName = parsed.scientificName,
            group = AnimalGroup.INVERTEBRATE,
            isAnimal = true,
            funFact = fact
        )
    }

    fun getInfoForGeneralModel(index: Int, rawLabel: String): AnimalInfo {
        val parsed = parseSpeciesLabel(rawLabel)
        val clean = parsed.displayName.lowercase()
        val group = detectGeneralGroup(index, clean)
        val isAnimal = group.isAnimal
        val funFact = getFact(clean, group)

        return AnimalInfo(
            displayName = parsed.displayName,
            scientificName = parsed.scientificName,
            group = group,
            isAnimal = isAnimal,
            funFact = funFact
        )
    }

    private fun detectGeneralGroup(index: Int, label: String): AnimalGroup {
        return when {
            index <= 0 || index > 398 -> AnimalGroup.NOT_ANIMAL
            (index in 1..7) || (index in 390..398) -> AnimalGroup.FISH
            (index in 8..25) || (index in 81..101) || (index in 128..147) -> AnimalGroup.BIRD
            index in 26..33 -> AnimalGroup.AMPHIBIAN
            index in 34..69 -> AnimalGroup.REPTILE
            (index in 70..80) || (index in 108..127) || (index in 301..330) -> AnimalGroup.INVERTEBRATE
            (index in 102..107) || (index in 148..300) || (index in 331..389) -> AnimalGroup.MAMMAL
            else -> AnimalGroup.NOT_ANIMAL
        }
    }

    private fun getFact(label: String, group: AnimalGroup): String {
        if (!group.isAnimal) {
            return "This appears to be an inanimate object ($label), not a living wildlife species."
        }

        return when {
            label.contains("dog") || label.contains("terrier") || label.contains("hound") ||
            label.contains("retriever") || label.contains("shepherd") || label.contains("spaniel") ||
            label.contains("bulldog") || label.contains("poodle") || label.contains("husky") ||
            label.contains("chihuahua") || label.contains("pug") || label.contains("corgi") ->
                "Canines possess up to 300 million olfactory receptors in their noses, compared to only 6 million in humans."

            label.contains("cat") || label.contains("tabby") || label.contains("siamese") || label.contains("persian") ->
                "Domestic felines can make over 100 distinct vocal sounds and have whiskers that detect subtle air currents."

            label.contains("lion") ->
                "Lions are the only social big cats, living in prides where females do roughly 85-90% of the pride's hunting."

            label.contains("tiger") ->
                "Tigers are solitary, powerful swimmers that can easily cross wide rivers, and no two have the same stripes."

            label.contains("cheetah") ->
                "The fastest land animal on Earth, capable of accelerating from 0 to 60 mph in roughly 3 seconds."

            label.contains("bear") || label.contains("panda") ->
                "Bears have an acute sense of smell estimated to be 7 times stronger than a bloodhound."

            label.contains("elephant") ->
                "Elephants are highly intelligent herbivores with complex matriarchal structures that communicate via infrasound."

            label.contains("hawk") || label.contains("eagle") || label.contains("falcon") ->
                "Raptors have incredible binocular vision capable of spotting tiny prey from miles up in the sky."

            label.contains("owl") ->
                "Owls can rotate their heads up to 270 degrees and have specialized serrated feathers for silent flight."

            label.contains("jay") || label.contains("crow") || label.contains("raven") || label.contains("magpie") ->
                "Corvids are among the most intelligent creatures on Earth, capable of facial recognition and tool fabrication."

            label.contains("finch") || label.contains("sparrow") || label.contains("warbler") ->
                "Songbirds have specialized vocal organs called syrinxes, allowing them to produce two distinct notes simultaneously."

            label.contains("woodpecker") ->
                "Woodpeckers possess shock-absorbing skulls and wrap their long tongues around their brain to cushion impacts."

            label.contains("hummingbird") ->
                "The only birds capable of flying backwards and upside down, flapping their wings up to 80 times per second."

            label.contains("beetle") ->
                "Beetles account for roughly 25% of all known animal life forms on Earth, with over 350,000 described species."

            label.contains("butterfly") || label.contains("moth") ->
                "Lepidoptera taste through chemoreceptors on their feet and navigate using polarized sunlight."

            label.contains("dragonfly") || label.contains("damselfly") ->
                "Dragonflies are apex aerial hunters with an astonishing 95% hunt success rate."

            label.contains("bee") || label.contains("wasp") ->
                "Vital pollinators that communicate floral resource locations through specialized 'waggle dances'."

            label.contains("spider") || label.contains("tarantula") ->
                "Arachnids produce silk that is tensilely stronger than high-grade steel of equal diameter."

            label.contains("snake") || label.contains("cobra") || label.contains("viper") || label.contains("python") ->
                "Snakes smell using their forked tongue and Jacobson's organ to follow invisible scent trails."

            label.contains("lizard") || label.contains("gecko") || label.contains("iguana") ->
                "Geckos can climb smooth vertical surfaces thanks to millions of microscopic hairs called setae on their toes."

            label.contains("turtle") || label.contains("tortoise") ->
                "Their protective shells are an integral part of their skeleton, fused directly with their ribs and spine."

            label.contains("frog") || label.contains("toad") || label.contains("salamander") ->
                "Amphibians undergo complete metamorphosis and absorb moisture and oxygen directly through their skin."

            label.contains("shark") ->
                "Ancient apex ocean predators with skeletons made entirely of flexible cartilage rather than bone."

            else -> when (group) {
                AnimalGroup.BIRD -> "Feathered, warm-blooded egg-laying vertebrates equipped with lightweight hollow bones."
                AnimalGroup.REPTILE -> "Ectothermic scaled vertebrates that thrive across terrestrial, desert, and aquatic habitats."
                AnimalGroup.AMPHIBIAN -> "Semiaquatic vertebrates that serve as crucial environmental indicators of healthy ecosystems."
                AnimalGroup.FISH -> "Aquatic gill-bearing vertebrates that navigate via lateral line sensory systems."
                AnimalGroup.INVERTEBRATE -> "Invertebrates make up over 95% of all animal biodiversity on planet Earth."
                AnimalGroup.MAMMAL -> "Warm-blooded vertebrates characterized by fur or hair and maternal care of their young."
                AnimalGroup.NOT_ANIMAL -> "Inanimate object detected."
            }
        }
    }
}
