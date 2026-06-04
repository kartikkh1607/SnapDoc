package com.kartik.snapdoc.domain.pipeline

/**
 * Typed reasons the photo pipeline can refuse to produce a result.
 *
 * Domain code never carries user-facing strings — the UI maps each reason
 * to a localized string resource. `Unexpected` is the catch-all for crashes
 * the pipeline didn't anticipate; its `devMessage` is for logs/telemetry only.
 */
sealed interface PipelineFailureReason {
    /** The source URI couldn't be opened or decoded into a bitmap. */
    data object SourceUnreadable : PipelineFailureReason

    /** ML Kit found no face in the source photo. */
    data object NoFaceDetected : PipelineFailureReason

    /** The face fills too much of the frame — user must step back. */
    data object FaceTooClose : PipelineFailureReason

    /** The final compressed JPEG couldn't be decoded for validation. */
    data object FinalDecodeFailed : PipelineFailureReason

    /** The doc id passed to the pipeline isn't in the spec catalog. */
    data object UnknownDocument : PipelineFailureReason

    /** Anything else — kept as a string for logs only, never for users. */
    data class Unexpected(val devMessage: String?) : PipelineFailureReason
}
