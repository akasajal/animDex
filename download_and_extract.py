import os
import io
import yaml
import urllib.request
from pathlib import Path
from PIL import Image
import pyarrow.parquet as pq
from huggingface_hub import hf_hub_download

DATASET_ROOT = Path('dataset')
TRAIN_DIR = DATASET_ROOT / 'train'
VAL_DIR = DATASET_ROOT / 'val'

print('1. Fetching exact 90-animal taxonomy mapping...')
url = 'https://huggingface.co/datasets/lucabaggi/animal-wildlife/raw/main/README.md'
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as resp:
    content = resp.read().decode('utf-8')
    meta = yaml.safe_load(content.split('---')[1])
    raw_class_dict = meta['dataset_info']['features'][1]['dtype']['class_label']['names']
    label_map = {int(k): v for k, v in raw_class_dict.items()}

print(f'Loaded {len(label_map)} animal classes.')

for name in label_map.values():
    (TRAIN_DIR / name).mkdir(parents=True, exist_ok=True)
    (VAL_DIR / name).mkdir(parents=True, exist_ok=True)

files_to_download = [
    ('data/test-00000-of-00001.parquet', VAL_DIR, 'Validation/Test'),
    ('data/train-00000-of-00002.parquet', TRAIN_DIR, 'Train Part 1'),
    ('data/train-00001-of-00002.parquet', TRAIN_DIR, 'Train Part 2'),
]

for filename, target_dir, label_desc in files_to_download:
    print(f'\nDownloading {label_desc} ({filename})...')
    local_path = hf_hub_download(
        repo_id='lucabaggi/animal-wildlife',
        filename=filename,
        repo_type='dataset'
    )
    print(f'Reading {local_path}...')
    table = pq.read_table(local_path)
    total_rows = len(table)
    print(f'Extracting {total_rows} images to {target_dir}...')

    for batch in table.to_batches(max_chunksize=200):
        batch_dict = batch.to_pydict()
        images = batch_dict['image']
        labels = batch_dict['label']

        for idx, (img_data, lbl_idx) in enumerate(zip(images, labels)):
            class_name = label_map[lbl_idx]
            img_bytes = img_data['bytes']
            out_file = target_dir / class_name / f'{batch.num_rows}_{idx}_{hash(img_bytes) & 0xFFFFFFFF}.jpg'
            if not out_file.exists():
                try:
                    img = Image.open(io.BytesIO(img_bytes)).convert('RGB')
                    img.save(out_file, 'JPEG', quality=90)
                except Exception as e:
                    print(f'Error saving image: {e}')

print('\nExtraction complete!')
print('Train classes:', len(list(TRAIN_DIR.iterdir())))
print('Val classes:', len(list(VAL_DIR.iterdir())))
