package no.ndla.taxonomysync.domain

import com.fasterxml.jackson.annotation.JsonProperty


class Authentication(
        @JsonProperty val access_token: String,
        @JsonProperty val scope: String,
        @JsonProperty val expires_in: Long,
        @JsonProperty val token_type: String)
