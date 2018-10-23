package com.mycompany.app

import com.mycompany.app.models.Job
import org.jdbi.v3.core.transaction.TransactionIsolationLevel
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule
import ru.vyarus.guicey.jdbi3.tx.TransactionTemplate
import ru.vyarus.guicey.jdbi3.tx.TxConfig
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread

class JobRepoTest {
    @get:Rule
    public var RULE = GuiceyAppRule(HelloWorldApplication::class.java, "example.yml")

    var template: TransactionTemplate? = null
    var jobDao: JobDao? = null

    @Before
    fun setup() {
        template = RULE.getBean(TransactionTemplate::class.java)
        jobDao = RULE.getBean(JobDao::class.java)
    }

    @Test
    fun shouldSaveAndLoad() {
        // Setup
        jobDao!!.deleteAll()
        val original = Job(name ="import", status = "QUEUED")

        // exercise
        val id = jobDao!!.insert(original)
        val actual = jobDao!!.getById(id)

        // assert
        val expected = original.copy(id = id)
        Assert.assertEquals("Job should save & load", expected, actual)
    }

    fun cancelOne(doThrow: Boolean, sleep: Int = 0, events: MutableList<String>? = null): Int? {
        // https://dba.stackexchange.com/questions/69471/postgres-update-limit-1
        val sql = """"
            update job
            set status='CANCELLING'
            where id = (
                select id
                from job
                where status = 'CANCEL_REQUESTED'
                order by cancel_request_time
                limit 1
                for update skip locked
            )
            returning id;
        """.trimMargin()

        // http://xvik.github.io/dropwizard-guicey/4.2.1/extras/jdbi3/
        var id: Int? = null
        template!!.inTransaction(TxConfig().level(TransactionIsolationLevel.REPEATABLE_READ)) { h ->
            // http://jdbi.org/#_fluent_api
            id = h.select(sql).mapTo(Int::class.javaPrimitiveType).findOnly()
            if(events != null) events.add("cancelling $id...")
            if(doThrow) throw RuntimeException("Kaboom!")
            Thread.sleep(sleep.toLong())
            if(events != null) events.add("cancelled $id")
        }
        return id
    }

    @Test
    fun shouldUseTransactions() {
        // setup
        jobDao!!.deleteAll()
        val idA = jobDao!!.insert(Job(name = "import", status = "CANCEL_REQUESTED", cancelRequestTime = Instant.now()))
        val idB = jobDao!!.insert(Job(name = "import", status = "CANCEL_REQUESTED", cancelRequestTime = Instant.now()))

        // exercise
        val cancelA = cancelOne(false)
        val cancelB = cancelOne(false)

        // assert
        Assert.assertEquals(idA, cancelA)
        Assert.assertEquals(idB, cancelB)
    }

    @Test
    fun shouldAllowConcurrency() {
        // setup
        jobDao!!.deleteAll()
        val idA = jobDao!!.insert(Job(name = "import", status = "CANCEL_REQUESTED", cancelRequestTime = Instant.now()))
        val idB = jobDao!!.insert(Job(name = "import", status = "CANCEL_REQUESTED", cancelRequestTime = Instant.now()))
        val events = Collections.synchronizedList(mutableListOf<String>())

        // exercise
        val threadA = thread {
            cancelOne(false, 50, events)
        }
        val threadB = thread {
            cancelOne(false, 50, events)
        }
        threadA.join()
        threadB.join()

        // assert
        Assert.assertEquals(events[0], "cancelling $idA...")
        Assert.assertEquals(events[1], "cancelling $idB...")
        Assert.assertEquals(events[2], "cancelled $idA")
        Assert.assertEquals(events[3], "cancelled $idB")
    }

    @Test
    fun shouldRollback() {
        jobDao!!.deleteAll()
        val id = jobDao!!.insert(Job(name = "import", status = "CANCEL_REQUESTED", cancelRequestTime = Instant.now()))

        // exercise
        try {
            cancelOne(true)
        } catch (ex: Exception) {
            
        }
        val job = jobDao!!.getById(id)

        // assert
        Assert.assertEquals("CANCEL_REQUESTED", job.status)
    }

}