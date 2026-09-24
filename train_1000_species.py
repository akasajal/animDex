import os
import shutil
from pathlib import Path
import tensorflow as tf
from tensorflow.keras import layers, models
import flatbuffers
from tflite_support import metadata
from tflite_support import metadata_schema_py_generated as _metadata_fb

IMG_SIZE = (224, 224)
BATCH_SIZE = 32
INITIAL_EPOCHS = 6
FINETUNE_EPOCHS = 2
DATASET_TRAIN = 'dataset_1000/train'
DATASET_VAL = 'dataset_1000/val'
MODEL_SAVE_PATH = 'saved_fauna1000_model.keras'
FINAL_TFLITE_PATH = 'animal_classifier.tflite'
ANDROID_ASSETS_PATH = r'E:\PROJECTS\animDex\app\src\main\assets\animal_classifier.tflite'

print('=== 1. Loading 1000-Species Dataset ===')
train_ds = tf.keras.utils.image_dataset_from_directory(
    DATASET_TRAIN,
    image_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    label_mode='categorical',
    shuffle=True,
    seed=42
)

val_ds = tf.keras.utils.image_dataset_from_directory(
    DATASET_VAL,
    image_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    label_mode='categorical',
    shuffle=False
)

class_names = train_ds.class_names
num_classes = len(class_names)
print(f'Detected {num_classes} living animal species in dataset!')

with open('labels_1000.txt', 'w', encoding='utf-8') as f:
    for name in class_names:
        f.write(name + '\n')
print('Generated labels_1000.txt')

AUTOTUNE = tf.data.AUTOTUNE
train_ds = train_ds.prefetch(buffer_size=AUTOTUNE)
val_ds = val_ds.prefetch(buffer_size=AUTOTUNE)

print('\n=== 2. Building Architecture (MobileNetV2 Backbone) ===')
data_augmentation = tf.keras.Sequential([
    layers.RandomFlip('horizontal'),
    layers.RandomRotation(0.1),
    layers.RandomZoom(0.1),
])

preprocess_input = tf.keras.applications.mobilenet_v2.preprocess_input

base_model = tf.keras.applications.MobileNetV2(
    input_shape=(224, 224, 3),
    include_top=False,
    weights='imagenet'
)
base_model.trainable = False

inputs = tf.keras.Input(shape=(224, 224, 3))
x = data_augmentation(inputs)
x = preprocess_input(x)
x = base_model(x, training=False)
x = layers.GlobalAveragePooling2D()(x)
x = layers.Dropout(0.3)(x)
outputs = layers.Dense(num_classes, activation='softmax')(x)

model = models.Model(inputs, outputs)

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
    loss='categorical_crossentropy',
    metrics=['accuracy', tf.keras.metrics.TopKCategoricalAccuracy(k=5, name='top_5_accuracy')]
)
model.summary()

print('\n=== 3. Phase 1 Training (Feature Extraction) ===')
history = model.fit(
    train_ds,
    validation_data=val_ds,
    epochs=INITIAL_EPOCHS,
    verbose=1
)

print('\n=== 4. Phase 2 Training (Fine-Tuning Top 20 Layers) ===')
base_model.trainable = True
for layer in base_model.layers[:-20]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
    loss='categorical_crossentropy',
    metrics=['accuracy', tf.keras.metrics.TopKCategoricalAccuracy(k=5, name='top_5_accuracy')]
)

history_finetune = model.fit(
    train_ds,
    validation_data=val_ds,
    epochs=FINETUNE_EPOCHS,
    verbose=1
)

model.save(MODEL_SAVE_PATH)
print(f'Model saved to {MODEL_SAVE_PATH}')

print('\n=== 5. Converting to TFLite (INT8 Quantization) ===')
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model_bytes = converter.convert()

raw_tflite = 'raw_fauna1000.tflite'
with open(raw_tflite, 'wb') as f:
    f.write(tflite_model_bytes)
print(f'Raw quantized TFLite size: {len(tflite_model_bytes) / (1024*1024):.2f} MB')

print('\n=== 6. Packaging Task Vision Metadata & Labels ===')
model_meta = _metadata_fb.ModelMetadataT()
model_meta.name = 'FaunaClassifier1000'
model_meta.version = 'v2.0'
model_meta.description = f'AnimDex {num_classes}-species living fauna classifier'

input_meta = _metadata_fb.TensorMetadataT()
input_meta.name = 'image'
input_meta.description = 'Input image (224x224 RGB)'
input_meta.content = _metadata_fb.ContentT()
input_meta.content.contentProperties = _metadata_fb.ImagePropertiesT()
input_meta.content.contentProperties.colorSpace = _metadata_fb.ColorSpaceType.RGB
input_meta.content.contentPropertiesType = _metadata_fb.ContentProperties.ImageProperties

input_normalization = _metadata_fb.ProcessUnitT()
input_normalization.optionsType = _metadata_fb.ProcessUnitOptions.NormalizationOptions
input_normalization.options = _metadata_fb.NormalizationOptionsT()
input_normalization.options.mean = [127.5]
input_normalization.options.std = [127.5]
input_meta.processUnits = [input_normalization]

output_meta = _metadata_fb.TensorMetadataT()
output_meta.name = 'probability'
output_meta.description = f'Probabilities for {num_classes} animal species'
output_meta.content = _metadata_fb.ContentT()
output_meta.content.contentPropertiesType = _metadata_fb.ContentProperties.FeatureProperties
output_meta.content.contentProperties = _metadata_fb.FeaturePropertiesT()

label_file = _metadata_fb.AssociatedFileT()
label_file.name = 'labels_1000.txt'
label_file.description = 'Labels for animal classes'
label_file.type = _metadata_fb.AssociatedFileType.TENSOR_AXIS_LABELS
output_meta.associatedFiles = [label_file]

subgraph = _metadata_fb.SubGraphMetadataT()
subgraph.inputTensorMetadata = [input_meta]
subgraph.outputTensorMetadata = [output_meta]
model_meta.subgraphMetadata = [subgraph]

b = flatbuffers.Builder(0)
b.Finish(model_meta.Pack(b), metadata.MetadataPopulator.METADATA_FILE_IDENTIFIER)

populator = metadata.MetadataPopulator.with_model_file(raw_tflite)
populator.load_metadata_buffer(b.Output())
populator.load_associated_files(['labels_1000.txt'])
populator.populate()

shutil.copy(raw_tflite, FINAL_TFLITE_PATH)
print(f'Populated TFLite model generated: {FINAL_TFLITE_PATH}')

if os.path.exists(os.path.dirname(ANDROID_ASSETS_PATH)):
    shutil.copy(FINAL_TFLITE_PATH, ANDROID_ASSETS_PATH)
    print(f'DEPLOYED: Copied model directly to {ANDROID_ASSETS_PATH}')

print('\n=== Training and Export Finished Successfully! ===')
