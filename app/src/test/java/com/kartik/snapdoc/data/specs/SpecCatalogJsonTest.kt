package com.kartik.snapdoc.data.specs

import com.google.common.truth.Truth.assertThat
import com.kartik.snapdoc.data.specs.model.SpecCatalog
import kotlinx.serialization.json.Json
import org.junit.Test

class SpecCatalogJsonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `parses a minimal valid catalog`() {
        val raw = """
            {
              "version": 1,
              "updatedAt": "2026-01-15",
              "categories": [
                { "id": "in_government", "displayName": "Indian Government", "icon": "ic_govt" }
              ],
              "documents": [
                {
                  "id": "in_passport",
                  "displayName": "Indian Passport",
                  "shortName": "Passport",
                  "categoryId": "in_government",
                  "popularity": 100,
                  "dimensions": { "widthMm": 35, "heightMm": 45, "widthPx": 413, "heightPx": 531, "dpi": 300 },
                  "background": { "colorHex": "#FFFFFF", "displayName": "White", "toleranceLab": 5 },
                  "face": { "headHeightPercentMin": 70, "headHeightPercentMax": 80, "eyeLineFromTopPercentMin": 50, "eyeLineFromTopPercentMax": 70 },
                  "file": { "format": "JPG", "minSizeKb": 10, "maxSizeKb": 100 },
                  "rules": { "glassesAllowed": false, "headCoveringAllowed": true, "mouthClosed": true, "neutralExpression": true, "eyesOpen": true, "noShadows": true }
                }
              ]
            }
        """.trimIndent()

        val catalog = json.decodeFromString<SpecCatalog>(raw)

        assertThat(catalog.version).isEqualTo(1)
        assertThat(catalog.documents).hasSize(1)
        val doc = catalog.documents.first()
        assertThat(doc.id).isEqualTo("in_passport")
        assertThat(doc.dimensions.widthPx).isEqualTo(413)
        assertThat(doc.dimensions.heightPx).isEqualTo(531)
        assertThat(doc.background.colorHex).isEqualTo("#FFFFFF")
        assertThat(doc.face.headHeightPercentMin).isEqualTo(70)
        assertThat(doc.file.maxSizeKb).isEqualTo(100)
    }

    @Test
    fun `tolerates extra unknown keys without failing`() {
        val raw = """
            {
              "version": 2,
              "updatedAt": "2026-06-01",
              "futureField": "unknown",
              "categories": [],
              "documents": []
            }
        """.trimIndent()

        val catalog = json.decodeFromString<SpecCatalog>(raw)
        assertThat(catalog.version).isEqualTo(2)
        assertThat(catalog.documents).isEmpty()
    }

    @Test(expected = Exception::class)
    fun `malformed JSON throws so loader can fall back`() {
        val raw = "{ this is not valid json"
        json.decodeFromString<SpecCatalog>(raw)
    }
}
