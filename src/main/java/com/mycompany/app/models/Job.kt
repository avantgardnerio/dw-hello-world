package com.mycompany.app.models

import java.time.Instant

data class Job(
        val id: Int? = null,
        val name: String,
        val status: String,
        val externalId: String? = null,
        val message: String? = null,
        val submitTime: Instant? = null,
        val startTime: Instant? = null,
        val endTime: Instant? = null,
        val cancelRequestTime: Instant? = null
)
