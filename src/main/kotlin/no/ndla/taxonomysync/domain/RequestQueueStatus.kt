package no.ndla.taxonomysync.domain

data class RequestQueueStatus(var currentRequest: TaxonomyApiRequest?, var currentAttempts: Int, var queuedItems: Int)
