package com.mycompany.app

import com.mycompany.app.models.Job
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule

class JobRepoTest {
    @get:Rule
    public var RULE = GuiceyAppRule(HelloWorldApplication::class.java, "example.yml")

    @Test
    fun shouldSaveAndLoad() {
        // Setup
        val jobDao = RULE.getBean(JobDao::class.java)
        jobDao.deleteAll()
        val original = Job(null, "import", "QUEUED", null, null, null, null, null, null)

        // exercise
        val id = jobDao.insert(original)
        val actual = jobDao.getById(id)

        // assert
        val expected = original.copy(id = id)
        Assert.assertEquals("Job should save & load", expected, actual)
    }

}