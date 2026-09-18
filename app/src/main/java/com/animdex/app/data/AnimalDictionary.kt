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
        "hyena" to Pair("Spotted Hyena", "Crocuta crocuta"),
        "cat" to Pair("Cat", "Felis catus"),
        "cow" to Pair("Domestic Cattle / Cow", "Bos taurus"),
        "sparrow" to Pair("Songbird / Sparrow", "Passeridae"),
        "lizard" to Pair("Lizard", "Lacertilia"),
        "toad" to Pair("Toad / Frog", "Bufonidae"),
        "woodpecker" to Pair("Woodpecker", "Picidae"),
        "owl" to Pair("Owl", "Strigiformes"),
        "pigeon" to Pair("Pigeon / Dove", "Columbidae"),
        "crow" to Pair("Crow / Raven", "Corvus"),
        "eagle" to Pair("Eagle", "Accipitridae"),
        "duck" to Pair("Duck", "Anatidae"),
        "swan" to Pair("Swan", "Cygnus"),
        "parrot" to Pair("Parrot", "Psittaciformes"),
        "hummingbird" to Pair("Hummingbird", "Trochilidae"),
        "snake" to Pair("Snake", "Serpentes"),
        "turtle" to Pair("Turtle / Tortoise", "Testudines"),
        "dog" to Pair("Domestic Dog", "Canis lupus familiaris")
    )

    fun getInfoForGeneralModel(index: Int, rawLabel: String): AnimalInfo {
        val parsed = parseSpeciesLabel(rawLabel)
        val clean = parsed.displayName.lowercase().trim()
        val mapped = GENERAL_TAXONOMY[clean] ?: GENERAL_TAXONOMY.entries.firstOrNull { clean.contains(it.key) }?.value

        val finalDisplayName = mapped?.first ?: parsed.displayName
        val finalScientificName = mapped?.second ?: parsed.scientificName

        val group = detectGeneralGroup(clean)
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

    private fun detectGeneralGroup(label: String): AnimalGroup {
        val l = label.lowercase()
        return when {
            // Inanimate objects / screens / furniture
            l.contains("screen") || l.contains("monitor") || l.contains("laptop") ||
            l.contains("television") || l.contains("phone") || l.contains("desk") ||
            l.contains("chair") || l.contains("table") || l.contains("car") ||
            l.contains("vehicle") || l.contains("bottle") || l.contains("cup") ||
            l.contains("keyboard") || l.contains("book") -> AnimalGroup.NOT_ANIMAL

            // Birds
            l.contains("bird") || l.contains("sparrow") || l.contains("woodpecker") ||
            l.contains("owl") || l.contains("pigeon") || l.contains("crow") ||
            l.contains("eagle") || l.contains("duck") || l.contains("swan") ||
            l.contains("parrot") || l.contains("hummingbird") || l.contains("goose") ||
            l.contains("flamingo") || l.contains("hornbill") || l.contains("penguin") ||
            l.contains("sandpiper") || l.contains("turkey") || l.contains("pelecaniformes") ||
            l.contains("cock") || l.contains("hen") || l.contains("finch") || l.contains("robin") -> AnimalGroup.BIRD

            // Reptiles
            l.contains("lizard") || l.contains("chameleon") || l.contains("snake") ||
            l.contains("turtle") || l.contains("tortoise") || l.contains("alligator") ||
            l.contains("crocodile") || l.contains("iguana") || l.contains("gecko") -> AnimalGroup.REPTILE

            // Amphibians
            l.contains("toad") || l.contains("frog") || l.contains("salamander") -> AnimalGroup.AMPHIBIAN

            // Fish
            l.contains("fish") || l.contains("goldfish") || l.contains("shark") ||
            l.contains("seahorse") -> AnimalGroup.FISH

            // Invertebrates & Insects
            l.contains("bee") || l.contains("beetle") || l.contains("butterfly") ||
            l.contains("caterpillar") || l.contains("cockroach") || l.contains("crab") ||
            l.contains("dragonfly") || l.contains("fly") || l.contains("grasshopper") ||
            l.contains("jellyfish") || l.contains("ladybug") || l.contains("lobster") ||
            l.contains("mosquito") || l.contains("moth") || l.contains("octopus") ||
            l.contains("oyster") || l.contains("spider") || l.contains("squid") ||
            l.contains("starfish") || l.contains("insect") || l.contains("worm") ||
            l.contains("ant") || l.contains("wasp") -> AnimalGroup.INVERTEBRATE

            // Mammals (Default for all other wildlife & pets)
            else -> AnimalGroup.MAMMAL
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
