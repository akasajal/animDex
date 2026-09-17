> Project in progress

# AnimDex - Wildlife Field Guide & Catalog

AnimDex is a native Android application built with Jetpack Compose that runs multiple specialized on-device TensorFlow Lite machine learning models to identify animals, birds, and insects with high accuracy and zero cloud dependencies. Designed as a real-world field companion, AnimDex incorporates on-device anti-spoofing heuristics to verify authentic wildlife captures in natural environments.

---

## Anti-Spoofing & Live Wildlife Verification

To preserve the authenticity of field discoveries and prevent registration of subjects captured from computer monitors, phone screens, or physical photo prints, AnimDex runs a multi-factor verification pipeline on every capture:

1. **Display & Media Classification**: Uses the general vision classifier to detect electronic displays (monitors, laptops, televisions, phones) and printed publications (magazines, books, photocopies, picture frames).
2. **Moiré Pattern & Subpixel Dispersion Analysis**: Identifies high-frequency spatial chromatic dispersion characteristic of LCD and OLED RGB pixel grids.
3. **Specular Glare & Backlight Pedestal**: Detects planar glass reflections and elevated black-level pedestals characteristic of backlit screens.
4. **Bezel & Frame Border Heuristics**: Analyzes capture perimeters for rectangular hardware bezels or paper border collinearity.

Captures identified as screens or prints are flagged with an unverified notice and locked from catalog registration.

---

## Multi-Model Vision Architecture

Instead of relying on a single generic model, AnimDex employs specialized vision models trained on biodiversity datasets:

```
                              [ Camera Frame ]
                                      │
                 ┌────────────────────┴────────────────────┐
                 │                                         │
        [ Manual Mode Selection ]                 [ Auto-Cascade Mode ]
                 │                                         │
   ┌─────────────┼─────────────┐                           ▼
   ▼             ▼             ▼                [ General Fauna Model ]
[ Birds ]   [ Insects ]   [ General ]                      │
   │             │             │            ┌──────────────┼──────────────┐
   │             │             │            ▼              ▼              ▼
   │             │             │       [ Inanimate ]    [ Bird? ]     [ Insect? ]
   │             │             │            │              │              │
   │             │             │       Alert Object        │              │
   │             │             │      (Filter Non-Fauna)   │              │
   │             │             │                           ▼              ▼
   ▼             ▼             ▼                    [ iNat Birds ]  [ iNat Insects ]
[ iNat Birds ] [ iNat Insects] [ General ]                 │              │
(965 species) (1,022 species) (Fauna & Wildlife)           ▼              ▼
   │             │             │                    Fine-Grained Species & Binomial
   └─────────────┬─────────────┘                           │
                 ▼                                         │
    [ Anti-Spoofing Verification ]                         │
                 │                                         │
                 ▼                                         │
    [ AnimDex Result & Catalog Persistence ] ◄─────────────┘
```

### Bundled On-Device Models (`app/src/main/assets/`)

- **Specialized Bird Model (`bird_classifier.tflite`)**:
  - Trained on iNaturalist Birds.
  - Identifies 965 avian species with common names and binomial scientific nomenclature (e.g., *Cyanocitta cristata* - Blue Jay).
- **Specialized Insect & Arthropod Model (`insect_classifier.tflite`)**:
  - Trained on iNaturalist Insects.
  - Identifies 1,022 invertebrate species including butterflies, beetles, dragonflies, bees, and spiders (e.g., *Danaus plexippus* - Monarch).
- **General Fauna Model (`animal_classifier.tflite`)**:
  - Identifies mammals, reptiles, amphibians, fish, and general wildlife.
  - Detects non-animal objects to filter out false-positive identifications.

---

## Scanner Controls & Modes

- **Auto Mode**:
  - Inspects the scene using the general fauna classifier.
  - Inanimate objects trigger an informational warning without guessing an animal.
  - Avian or invertebrate subjects automatically cascade into dedicated iNaturalist models for fine-grained identification.
- **Birds Mode**:
  - Routes directly to the 965-species bird classifier.
- **Insects Mode**:
  - Routes directly to the 1,022-species insect and arthropod classifier.
- **General Wildlife Mode**:
  - Routes to the fauna classifier covering mammals, reptiles, amphibians, and fish.

---

## Local AnimDex Catalog (Room SQLite)

- Stores discovered wildlife locally on device.
- Metadata recorded:
  - Species common name (e.g., Cassin's Finch)
  - Binomial scientific name (e.g., *Haemorhous cassinii*)
  - Catalog identification engine attribution
  - Taxonomic group (Mammal, Bird, Reptile, Amphibian, Fish, Invertebrate)
  - Identification confidence percentage
  - Natural history notes and fun facts
  - Photo snapshot and discovery timestamp

---

## Personalization & Settings

- **Theme Mode**: System default, Dark theme, or Light theme with uniform segmented selection.
- **Accent Color Palette**: Six custom Material 3 accent palettes (Rose, Lavender, Sage, Sky, Amber, Slate).
- **Settings Persistence**: Theme, accent colors, and preferred scanner modes persist across app restarts using SharedPreferences.

---

## Tech Stack

- **UI**: Jetpack Compose (Material 3) with extended vector iconography
- **Camera**: AndroidX CameraX (Lifecycle, Camera2, View)
- **Machine Learning**: TensorFlow Lite Task Vision (`org.tensorflow:tensorflow-lite-task-vision:0.4.4`)
- **Persistence**: Room Database with KSP, SharedPreferences
- **Image Loading**: Coil Compose
- **Language**: Kotlin with Coroutines & StateFlow
