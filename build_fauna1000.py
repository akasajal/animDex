import os
import io
import json
import shutil
from pathlib import Path
from PIL import Image
import pyarrow.parquet as pq
from huggingface_hub import hf_hub_download

TARGET_TAX_PATH = 'inat_fauna_taxonomy.json'
OUT_DIR = Path('dataset_1000')
TRAIN_DIR = OUT_DIR / 'train'
VAL_DIR = OUT_DIR / 'val'

print('=== 1. Loading iNaturalist Fauna Taxonomy ===')
with open(TARGET_TAX_PATH, 'r', encoding='utf-8') as f:
    tax = json.load(f)

tax_by_id = {int(k): v for k, v in tax.items()}
print(f'Loaded {len(tax_by_id)} target fauna species.')

TRAIN_DIR.mkdir(parents=True, exist_ok=True)
VAL_DIR.mkdir(parents=True, exist_ok=True)

def get_clean_name(info):
    cn = info.get('common_name') or ''
    sn = info['scientific_name']
    if cn and cn.lower() != sn.lower():
        clean = f'{cn} ({sn})'
    else:
        clean = sn
    for ch in ['/', '\\', ':', '*', '?', '"', '<', '>', '|']:
        clean = clean.replace(ch, '_')
    return clean

NUM_SHARDS = 6
species_counts = {}

for shard_idx in range(NUM_SHARDS):
    filename = f'data/train-{shard_idx:05d}-of-00017.parquet'
    print(f'\n[Shard {shard_idx + 1}/{NUM_SHARDS}] Downloading/Reading {filename}...')
    try:
        path = hf_hub_download(
            repo_id='MVRL/iNat-2021-val',
            filename=filename,
            repo_type='dataset'
        )
    except Exception as e:
        print(f'Error downloading shard {shard_idx}: {e}')
        continue

    print(f'Extracting fauna images from {filename}...')
    table = pq.read_table(path, columns=['category_id', 'image', 'file_name'])
    for batch in table.to_batches(max_chunksize=300):
        b_dict = batch.to_pydict()
        cids = b_dict['category_id']
        imgs = b_dict['image']
        fnames = b_dict['file_name']

        for cid, img_dict, fname in zip(cids, imgs, fnames):
            if cid not in tax_by_id:
                continue

            info = tax_by_id[cid]
            class_name = get_clean_name(info)
            curr_count = species_counts.get(class_name, 0)
            species_counts[class_name] = curr_count + 1

            target_folder = VAL_DIR / class_name if (curr_count % 5 == 0) else TRAIN_DIR / class_name
            target_folder.mkdir(parents=True, exist_ok=True)

            img_bytes = img_dict['bytes']
            out_file = target_folder / f'{Path(fname).stem}.jpg'
            if not out_file.exists():
                try:
                    img = Image.open(io.BytesIO(img_bytes)).convert('RGB')
                    img.save(out_file, 'JPEG', quality=90)
                except Exception as e:
                    pass

print(f'\nExtracted iNaturalist images for {len(species_counts)} fauna species.')

print('\n=== 2. Merging Animals-90 (Domestic Pets & Animals) ===')
source_90_train = Path('dataset/train')
source_90_val = Path('dataset/val')

if source_90_train.exists():
    for cdir in source_90_train.iterdir():
        if not cdir.is_dir(): continue
        clean_cname = ' '.join([w.capitalize() for w in cdir.name.split('_')])
        dest_train = TRAIN_DIR / clean_cname
        dest_train.mkdir(parents=True, exist_ok=True)
        for img_f in cdir.glob('*.jpg'):
            shutil.copy(img_f, dest_train / img_f.name)

if source_90_val.exists():
    for cdir in source_90_val.iterdir():
        if not cdir.is_dir(): continue
        clean_cname = ' '.join([w.capitalize() for w in cdir.name.split('_')])
        dest_val = VAL_DIR / clean_cname
        dest_val.mkdir(parents=True, exist_ok=True)
        for img_f in cdir.glob('*.jpg'):
            shutil.copy(img_f, dest_val / img_f.name)

all_train_classes = sorted([d.name for d in TRAIN_DIR.iterdir() if d.is_dir()])
all_val_classes = sorted([d.name for d in VAL_DIR.iterdir() if d.is_dir()])
print(f'\nTotal Unique Classes in dataset_1000/train: {len(all_train_classes)}')
print(f'Total Unique Classes in dataset_1000/val: {len(all_val_classes)}')

total_train = sum(len(list(d.glob('*.jpg'))) for d in TRAIN_DIR.iterdir())
total_val = sum(len(list(d.glob('*.jpg'))) for d in VAL_DIR.iterdir())
print(f'Total Training Images: {total_train}')
print(f'Total Validation Images: {total_val}')

with open('labels_1000.txt', 'w', encoding='utf-8') as f:
    for name in all_train_classes:
        f.write(name + '\n')
print(f'Saved labels_1000.txt ({len(all_train_classes)} species).')
