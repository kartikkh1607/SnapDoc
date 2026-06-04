package com.kartik.snapdoc.ui.screens.doc_detail

import com.kartik.snapdoc.data.specs.model.DocumentSpec

data class DocDetailUiState(
    val doc: DocumentSpec? = null,
    val notFound: Boolean = false,
)
