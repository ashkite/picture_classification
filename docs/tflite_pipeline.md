# TFLite Training Pipeline (MVP)

Goal: event classification on-device with high precision (low false positives).

## Model choice
- Base: EfficientNet-Lite0 (input 224x224)
- Output: multi-label categories (sigmoid)
- Loss: BCE or Focal (precision-friendly)

## Data sources
- Places365 (scene)
- Open Images (objects)
- Food-101 (food)
- Public event/scene sets for weddings/parties/concerts

## Label mapping
- Map dataset labels to the event taxonomy in `docs/label_mapping.md`.
- Prefer conservative mapping (unknown > wrong label).

## Preprocess
- Resize to 224x224
- Normalize to [0,1] or ImageNet mean/std
- Class-imbalance handling: class weights or focal loss

## Train
- Optimizer: AdamW
- LR: 1e-3 warmup -> cosine decay
- Augment: light color jitter, random crop, horizontal flip
- Early stopping on validation precision

## Evaluate
- Track per-class precision/recall
- Global metric: mean precision at threshold 0.8
- Pick threshold per-class if needed

## Export
- Freeze + TFLite convert
- Quantize: int8 (calibration with representative dataset)
- Validate accuracy after conversion

## On-device inference
- TFLite Interpreter + NNAPI (optional)
- Batch inference in WorkManager
- Threshold: default 0.8, prefer no-tag over wrong tag

## Update strategy
- v1: bundle model in APK
- v2: Play Asset Delivery for model updates
