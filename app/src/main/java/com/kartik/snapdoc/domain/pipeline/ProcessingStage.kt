package com.kartik.snapdoc.domain.pipeline

enum class ProcessingStage(val label: String) {
    DetectingFace("Detecting face"),
    RemovingBackground("Removing background"),
    ApplyingBackground("Applying background color"),
    Cropping("Cropping to size"),
    Resizing("Resizing to spec"),
    Compressing("Compressing file"),
    Validating("Validating against spec"),
    Done("Done"),
}
