package com.mycompany.app.models

import java.time.Instant

data class Job(
        val id: Int?,
        val name: String,
        val status: String,
        val externalId: String?,
        val message: String?,
        val submitTime: Instant?,
        val startTime: Instant?,
        val endTime: Instant?,
        val cancelRequestTime: Instant?
)
