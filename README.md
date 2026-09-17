# AnimDex 🐾 - Multi-Model Machine Learning Animal Recognition App

**AnimDex** is a native Android application built with **Jetpack Compose** that runs **multiple specialized on-device TensorFlow Lite Machine Learning models** to recognize animals, birds, and insects with high accuracy and zero cloud dependencies.

---

## 🧠 Multi-Model Machine Learning Architecture

Instead of generic guessing or single-model limitations, AnimDex uses a **hierarchical multi-model ML pipeline**:

```
                              [ Captured Image ]
                                      │
                 ┌────────────────────┴────────────────────┐
                 │                                         │
        [ Manual Mode Selection ]                 [ Auto-Ensemble Mode ]
                 │                                         │
   ┌─────────────┼─────────────┐                           ▼
   ▼             ▼             ▼                [ General Fauna Model ]
[ Birds ]   [ Insects ]   [ General ]                      │
   │             │             │            ┌──────────────┼──────────────┐
   │             │             │            ▼              ▼              ▼
   │             │             │       [ Inanimate ]    [ Bird? ]     [ Insect? ]
   │             │             │            │              │              │
   │             │             │      Stop & Alert         │              │
   │             │             │    "Inanimate Object"     │              │
   │             │             │                           ▼              ▼
   ▼             ▼             ▼                    [ iNat Birds ]  [ iNat Insects]
[ iNat Birds ] [ iNat Insects] [ General ]                 │              │
(965 species) (1,022 species) (Fauna & Wildlife)           ▼              ▼
   │             │             │                    Fine-Grained Species & Binomial
   └─────────────┬─────────────┘                           │
                 ▼                                         │
    [ AnimDex Result Card & AnimDex Persistence ] ◄────────┘
```

### Bundled On-Device Models (`app/src/main/assets/`)
1. **Specialized Bird Model (`bird_classifier.tflite`)**:
   - Trained on **iNaturalist Birds**.
   - Identifies **965 avian species** with common names and binomial scientific nomenclature (e.g., *Cyanocitta cristata* - Blue Jay).
2. **Specialized Insect & Arthropod Model (`insect_classifier.tflite`)**:
   - Trained on **iNaturalist Insects**.
   - Identifies **1,022 invertebrate species** including butterflies, beetles, dragonflies, bees, and spiders (e.g., *Danaus plexippus* - Monarch).
3. **General Fauna Model (`animal_classifier.tflite`)**:
   - Identifies mammals, reptiles, amphibians, fish, and general wildlife.
   - Evaluates frame contents to detect non-animal objects (e.g., laptops, bottles, desks) and prevent false-positive classifications.

---

## 📱 Scanner Controls & Modes

- **⚡ Auto-Ensemble Mode**:
  - Evaluates the scene with the general fauna model.
  - If a non-animal object is detected, warns the user without guessing an animal.
  - If an avian or invertebrate specimen is detected, cascades automatically into the specialized iNaturalist expert models for deep species classification.
- **🪶 Birds Mode**:
  - Runs the specialized 965-species bird classifier directly.
- **🦋 Insects & Bugs Mode**:
  - Runs the specialized 1,022-species insect classifier directly.
- **🐾 General Wildlife Mode**:
  - Runs the fauna classifier covering mammals, reptiles, and aquatic wildlife.

---

## 💾 Local AnimDex Collection (Room SQLite)

- Saves discovered species to your on-device database.
- Stores:
  - Common Name (e.g., Cassin's Finch)
  - Binomial Scientific Name (e.g., *Haemorhous cassinii*)
  - Machine Learning Model Attribution (e.g., *iNaturalist Bird Model*)
  - Taxonomic Group badge (🐾 Mammal, 🪶 Bird, 🦎 Reptile, 🐸 Amphibian, 🐟 Fish, 🦋 Invertebrate)
  - Match Confidence %
  - Ecological bio / fun fact
  - Photo snapshot & timestamp

---

## 🛠️ Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Camera**: AndroidX CameraX (Lifecycle, Camera2, View)
- **Machine Learning**: TensorFlow Lite Task Vision (`org.tensorflow:tensorflow-lite-task-vision:0.4.4`)
- **Persistence**: Room Database 2.6+ with KSP
- **Image Loading**: Coil Compose 2.7+
- **Language**: Kotlin 2.0+ with Coroutines & StateFlow
