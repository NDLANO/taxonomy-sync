package no.ndla.taxonomysync.domain

class RequestQueueStatus(var currentRequest: TaxonomyApiRequest?, var currentAttempts: Int, var queuedItems: Int)
