package no.ndla.taxonomysync.domain

import no.ndla.taxonomysync.dtos.TaxonomyApiRequest

class RequestQueueStatus(var currentRequest: TaxonomyApiRequest?, var currentAttempts: Int, var queuedItems: Int)
