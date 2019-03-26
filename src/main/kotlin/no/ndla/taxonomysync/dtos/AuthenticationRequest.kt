package no.ndla.taxonomysync.dtos

data class AuthenticationRequest(val client_id:String,
                                 val client_secret: String,
                                 val grant_type: String = "client_credentials",
                                 val audience: String = "ndla_system")