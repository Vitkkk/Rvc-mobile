# RVC Mobile

Android-first implementation of Retrieval-based Voice Conversion.

## Architecture

RVC Mobile is a native Android application rather than the desktop RVC WebUI placed inside a WebView. The desktop project depends on Python/PyTorch, FAISS, audio-science packages and optional CUDA paths which are not assumed to exist on Android.

The mobile architecture is split into:

- Kotlin + Jetpack Compose + Material 3 UI
- Android Storage Access Framework / share intents for user files
- ARM64-first runtime
- a clean `RvcEngine` boundary so inference backends can evolve independently from the UI
- ONNX/native runtimes for portable inference components
- optional downloadable AI weights rather than a huge base APK
- CPU fallback with hardware-specific acceleration added only where it is actually supported

## Implemented so far

### Phase 1 foundation

- native Android project targeting `arm64-v8a`
- touch-first home navigation
- Voice Conversion screen foundation with audio picker, pitch, index-rate and protect controls
- persistent Voice Models library
- import of `.pth`, `.index` and `.zip`
- automatic ZIP scanning for RVC checkpoint/index files
- pairing separately imported PTH and INDEX files with the same model name
- import through Android file picker
- Android share-to-RVC-Mobile flow for model files
- model storage usage display and deletion
- device profiling for RAM, free storage, Vulkan availability and a recommended performance/training profile
- inference engine contract ready for Android-native backends
- GitHub Actions debug APK build

## Model compatibility strategy

Common RVC ecosystem files remain user-facing inputs (`.pth`, `.index`, `.zip`). Checkpoints are preserved when imported. Android inference may create a private runtime cache such as ONNX/ORT/native tensors, but the app should not require users to convert their model manually or adopt a proprietary replacement format.

RVC v1/v2 and F0-guided models remain primary compatibility targets. RMVPE is the first planned pitch backend.

## Next work

### Phase 2

- inspect/convert supported RVC checkpoints into an Android executable graph
- RMVPE Android runtime
- content feature extractor (HuBERT/ContentVec-compatible path)
- synthesizer inference
- retrieval/index implementation
- FFmpeg/media decoding and WAV export
- conversion progress, foreground processing and A/B player

### Later phases

- AI Cover and vocal separation
- dataset recorder/import/preprocessing
- on-device training experiments and checkpoint resume
- index creation
- thermal throttling and background training service
- GPU/runtime optimization and benchmark profiles

## Build

Initial target: `arm64-v8a`.

The repository currently uses a system Gradle build in CI:

```bash
gradle :app:assembleDebug
```

GitHub Actions runs the same build on pushes to `main` and uploads `RVC-Mobile-debug` when compilation succeeds.

A Gradle Wrapper can be committed once generated from a standard Gradle installation; until then the CI workflow pins the Gradle version explicitly.
