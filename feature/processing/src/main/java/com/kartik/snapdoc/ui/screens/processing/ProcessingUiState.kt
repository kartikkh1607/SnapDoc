package com.kartik.snapdoc.ui.screens.processing

import com.kartik.snapdoc.domain.pipeline.PipelineFailureReason
import com.kartik.snapdoc.domain.pipeline.ProcessingStage

data class ProcessingUiState(
    val stage: ProcessingStage = ProcessingStage.DetectingFace,
    val progress: Float = 0f,
    val error: PipelineFailureReason? = null,
    val resultUri: String? = null,
)
