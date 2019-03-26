package no.ndla.taxonomysync.domain

data class TaxonomyApiRequest(val timestamp: String, val method:String, val path: String, val body: String) : Queueable