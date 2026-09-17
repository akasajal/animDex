package com.animdex.app.data

import com.animdex.app.R

enum class AnimalGroup(
    val displayName: String,
    val iconRes: Int,
    val emoji: String,
    val isAnimal: Boolean = true
) {
    MAMMAL("Mammal", R.drawable.ic_paw, "🐾", true),
    BIRD("Bird", R.drawable.ic_bird, "🪶", true),
    REPTILE("Reptile", R.drawable.ic_reptile, "🦎", true),
    AMPHIBIAN("Amphibian", R.drawable.ic_amphibian, "🐸", true),
    FISH("Fish", R.drawable.ic_fish, "🐟", true),
    INVERTEBRATE("Invertebrate", R.drawable.ic_butterfly, "🦋", true),
    NOT_ANIMAL("Inanimate Object", R.drawable.ic_inanimate, "📦", false)
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

    private val GENERAL_TAXONOMY = mapOf(
        "egyptian cat" to Pair("Tabby Cat (Egyptian Mau)", "Felis catus"),
        "tabby" to Pair("Tabby Cat", "Felis catus"),
        "tabby cat" to Pair("Tabby Cat", "Felis catus"),
        "tiger cat" to Pair("Tabby Cat (Mackerel)", "Felis catus"),
        "persian cat" to Pair("Persian Cat", "Felis catus"),
        "siamese cat" to Pair("Siamese Cat", "Felis catus"),
        "cougar" to Pair("Cougar / Mountain Lion", "Puma concolor"),
        "lynx" to Pair("Lynx", "Lynx lynx"),
        "leopard" to Pair("Leopard", "Panthera pardus"),
        "snow leopard" to Pair("Snow Leopard", "Panthera uncia"),
        "jaguar" to Pair("Jaguar", "Panthera onca"),
        "lion" to Pair("Lion", "Panthera leo"),
        "tiger" to Pair("Tiger", "Panthera tigris"),
        "cheetah" to Pair("Cheetah", "Acinonyx jubatus"),
        "brown bear" to Pair("Brown Bear / Grizzly", "Ursus arctos"),
        "polar bear" to Pair("Polar Bear", "Ursus maritimus"),
        "sloth bear" to Pair("Sloth Bear", "Melursus ursinus"),
        "giant panda" to Pair("Giant Panda", "Ailuropoda melanoleuca"),
        "red fox" to Pair("Red Fox", "Vulpes vulpes"),
        "grey fox" to Pair("Grey Fox", "Urocyon cinereoargenteus"),
        "arctic fox" to Pair("Arctic Fox", "Vulpes lagopus"),
        "kit fox" to Pair("Kit Fox", "Vulpes macrotis"),
        "timber wolf" to Pair("Grey Wolf", "Canis lupus"),
        "white wolf" to Pair("Arctic Wolf", "Canis lupus arctos"),
        "coyote" to Pair("Coyote", "Canis latrans"),
        "golden retriever" to Pair("Golden Retriever", "Canis lupus familiaris"),
        "german shepherd" to Pair("German Shepherd", "Canis lupus familiaris"),
        "labrador retriever" to Pair("Labrador Retriever", "Canis lupus familiaris"),
        "beagle" to Pair("Beagle", "Canis lupus familiaris"),
        "boxer" to Pair("Boxer", "Canis lupus familiaris"),
        "rottweiler" to Pair("Rottweiler", "Canis lupus familiaris"),
        "dalmatian" to Pair("Dalmatian", "Canis lupus familiaris"),
        "collie" to Pair("Collie", "Canis lupus familiaris"),
        "african elephant" to Pair("African Elephant", "Loxodonta africana"),
        "indian elephant" to Pair("Asian Elephant", "Elephas maximus"),
        "hippopotamus" to Pair("Hippopotamus", "Hippopotamus amphibius"),
        "zebra" to Pair("Zebra", "Equus quagga"),
        "giraffe" to Pair("Giraffe", "Giraffa camelopardalis"),
        "bison" to Pair("American Bison", "Bison bison"),
        "ox" to Pair("Wild Ox / Cattle", "Bos taurus"),
        "water buffalo" to Pair("Water Buffalo", "Bubalus bubalis"),
        "wild boar" to Pair("Wild Boar", "Sus scrofa"),
        "warthog" to Pair("Warthog", "Phacochoerus africanus"),
        "red deer" to Pair("Red Deer", "Cervus elaphus"),
        "white-tailed deer" to Pair("White-Tailed Deer", "Odocoileus virginianus"),
        "moose" to Pair("Moose / Elk", "Alces alces"),
        "gazelle" to Pair("Gazelle", "Gazella"),
        "impala" to Pair("Impala", "Aepyceros melampus"),
        "llama" to Pair("Llama", "Lama glama"),
        "alpaca" to Pair("Alpaca", "Vicugna pacos"),
        "camel" to Pair("Camel", "Camelus"),
        "chimpanzee" to Pair("Chimpanzee", "Pan troglodytes"),
        "gorilla" to Pair("Western Gorilla", "Gorilla gorilla"),
        "orangutan" to Pair("Orangutan", "Pongo"),
        "koala" to Pair("Koala", "Phascolarctos cinereus"),
        "kangaroo" to Pair("Red Kangaroo", "Osphranter rufus"),
        "wallaby" to Pair("Wallaby", "Notamacropus"),
        "platypus" to Pair("Duck-Billed Platypus", "Ornithorhynchus anatinus"),
        "echidna" to Pair("Short-Beaked Echidna", "Tachyglossus aculeatus"),
        "otter" to Pair("River Otter", "Lontra canadensis"),
        "sea otter" to Pair("Sea Otter", "Enhydra lutris"),
        "badger" to Pair("European Badger", "Meles meles"),
        "honey badger" to Pair("Honey Badger", "Mellivora capensis"),
        "skunk" to Pair("Striped Skunk", "Mephitis mephitis"),
        "raccoon" to Pair("Common Raccoon", "Procyon lotor"),
        "red panda" to Pair("Red Panda", "Ailurus fulgens"),
        "beaver" to Pair("North American Beaver", "Castor canadensis"),
        "porcupine" to Pair("Crested Porcupine", "Hystrix cristata"),
        "squirrel" to Pair("Tree Squirrel", "Sciurus"),
        "chipmunk" to Pair("Eastern Chipmunk", "Tamias striatus"),
        "hedgehog" to Pair("European Hedgehog", "Erinaceus europaeus"),
        "rabbit" to Pair("European Rabbit", "Oryctolagus cuniculus"),
        "hare" to Pair("European Hare", "Lepus europaeus"),
        "american alligator" to Pair("American Alligator", "Alligator mississippiensis"),
        "nile crocodile" to Pair("Nile Crocodile", "Crocodylus niloticus"),
        "green iguana" to Pair("Green Iguana", "Iguana iguana"),
        "chameleon" to Pair("Chameleon", "Chamaeleonidae"),
        "komodo dragon" to Pair("Komodo Dragon", "Varanus komodoensis"),
        "leatherback turtle" to Pair("Leatherback Sea Turtle", "Dermochelys coriacea"),
        "green sea turtle" to Pair("Green Sea Turtle", "Chelonia mydas"),
        "great white shark" to Pair("Great White Shark", "Carcharodon carcharias"),
        "tiger shark" to Pair("Tiger Shark", "Galeocerdo cuvier"),
        "hammerhead shark" to Pair("Hammerhead Shark", "Sphyrna"),
        "killer whale" to Pair("Orca / Killer Whale", "Orcinus orca"),
        "blue whale" to Pair("Blue Whale", "Balaenoptera musculus"),
        "humpback whale" to Pair("Humpback Whale", "Megaptera novaeangliae"),
        "bottlenose dolphin" to Pair("Common Bottlenose Dolphin", "Tursiops truncatus"),
        "cock" to Pair("Rooster / Cockerel", "Gallus gallus domesticus"),
        "hen" to Pair("Hen / Chicken", "Gallus gallus domesticus"),
        "tusker" to Pair("Wild Elephant", "Loxodonta / Elephas"),
        "african hunting dog" to Pair("African Wild Dog", "Lycaon pictus"),
        "hyena" to Pair("Spotted Hyena", "Crocuta crocuta")
    )

    fun getInfoForGeneralModel(index: Int, rawLabel: String): AnimalInfo {
        val parsed = parseSpeciesLabel(rawLabel)
        val clean = parsed.displayName.lowercase().trim()
        val mapped = GENERAL_TAXONOMY[clean] ?: GENERAL_TAXONOMY.entries.firstOrNull { clean.contains(it.key) }?.value

        val finalDisplayName = mapped?.first ?: parsed.displayName
        val finalScientificName = mapped?.second ?: parsed.scientificName

        val group = detectGeneralGroup(index, clean)
        val isAnimal = group.isAnimal
        val funFact = getFact(clean, group)

        return AnimalInfo(
            displayName = finalDisplayName,
            scientificName = finalScientificName,
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
