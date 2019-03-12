package no.ndla.taxonomysync.dtos

data class TaxonomyApiRequest(val timestamp: String, val method:String, val path: String, val body: String)