package no.nav.tilbakekreving.tvangsgrunnlag.modell

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    Low, Medium, High, Vital
}

@Serializable
data class Tvangsgrunnlag(
    val id: String,
    val description: String,
    val priority: Priority
)

