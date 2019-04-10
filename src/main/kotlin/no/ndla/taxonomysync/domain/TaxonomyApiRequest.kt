package no.ndla.taxonomysync.domain

data class TaxonomyApiRequest(val timestamp: String, val method:String, val path: String, var body: String?) : Queueable