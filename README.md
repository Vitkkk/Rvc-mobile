# RVC Mobile

Android-first implementation of Retrieval-based Voice Conversion.

## Architecture decision

RVC Mobile is built as a native Android application rather than a desktop WebUI wrapped in WebView. The original RVC pipeline depends heavily on desktop Python packages such as PyTorch, FAISS, librosa/parselmouth, and optional CUDA paths. Those dependencies are not treated as Android requirements.

The mobile architecture is split into:

- Kotlin + Jetpack Compose + Material 3 UI
- Android Storage Access Framework for user-selected files/folders
- ARM64-first native runtime
- ONNX Runtime / native backends for portable inference components
- downloadable optional model assets rather than bundling large AI weights in the APK
- CPU fallback with room for NNAPI/Vulkan-compatible execution providers when practical

## Compatibility goals

Model import will preserve common RVC ecosystem files (`.pth`, `.index`, `.zip`). Imported checkpoints are treated as source model packages. Android inference may require conversion/caching to a portable runtime representation; the app should not invent a proprietary user-facing model format when avoidable.

RVC v1/v2 compatibility remains a primary target. The upstream implementation uses different synthesizer input dimensions for v1 and v2 and optional F0-guided variants, while the retrieval stage uses an index over extracted content features.

## Roadmap

### Phase 1
- Native Android application
- Touch-first home UI
- File management foundation
- RVC model import/library foundation

### Phase 2
- RVC inference runtime
- RMVPE
- Audio conversion/export

### Phase 3
- AI Cover workflow
- Vocal separation and remixing

### Phase 4
- Dataset creation and preprocessing

### Phase 5
- On-device training experiments, checkpoints and index creation

### Phase 6
- GPU/runtime optimization, memory reduction and benchmarks

## Build

Initial target is `arm64-v8a`.

```bash
./gradlew assembleDebug
```

The repository is currently in early Phase 1 and does not yet contain the complete inference or training runtime.
