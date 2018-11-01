package com.mycompany.app

import com.mesosphere.mesos.rx.java.SinkOperation
import com.mesosphere.mesos.rx.java.protobuf.ProtoUtils
import com.mesosphere.mesos.rx.java.protobuf.ProtobufMesosClientBuilder
import com.mesosphere.mesos.rx.java.protobuf.SchedulerCalls
import com.mesosphere.mesos.rx.java.util.UserAgentEntry
import org.apache.mesos.v1.Protos
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class MesosTest {

    var master: Process? = null
    var slave: Process? = null

    @Before
    fun setup() {
        val path = System.getenv("PATH").split(":") + "/usr/local/sbin"
        val masterPath = path
                .map { Paths.get(it, "mesos-master") }
                .find { Files.exists(it) }
        val slavePath = path
                .map { Paths.get(it, "mesos-slave") }
                .find { Files.exists(it) }
        master = Runtime.getRuntime().exec("$masterPath --registry=in_memory --ip=127.0.0.1 --work_dir=/tmp/mesos/master --advertise_ip=127.0.0.1 --hostname=127.0.0.1 --zk=zk://127.0.0.1:2181/mesos")
        slave = Runtime.getRuntime().exec("$slavePath --master=127.0.0.1:5050 --ip=127.0.0.1 --work_dir=/tmp/mesos/slave --launcher=posix --hostname=127.0.0.1 --advertise_ip=127.0.0.1")
    }

    @After
    fun tearDown() {
        master!!.destroy()
        slave!!.destroy()
    }

    @Test
    fun `should schedule jobs`() {
        val mesosUri = URI.create("http://localhost:5050/api/v1/scheduler")
        val role = "*"
        val fwId = UUID.randomUUID().toString()
        val frameworkID = Protos.FrameworkID.newBuilder().setValue(fwId).build()

        val clientBuilder = ProtobufMesosClientBuilder.schedulerUsingProtos()
                .mesosUri(mesosUri)
                .applicationUserAgentEntry { UserAgentEntry("my-app", "1.0") }

        val fwInfo = Protos.FrameworkInfo.newBuilder()
                .setId(frameworkID)
                .setUser(Optional.ofNullable(System.getenv("user")).orElse("root")) // https://issues.apache.org/jira/browse/MESOS-3747
                .setName("myApp")
                .setFailoverTimeout(0.0)
                .setRole(role)
                .build()
        val subscribeCall = SchedulerCalls.subscribe(frameworkID, fwInfo)

        clientBuilder
                .subscribe(subscribeCall)
                .processStream { unicastEvents ->
                    val events = unicastEvents.share()
                    val errorLogger = events
                            .doOnNext { e -> println(e) }
                            .map { e -> Optional.empty<SinkOperation<org.apache.mesos.v1.scheduler.Protos.Call>>() }
                    errorLogger
                }

        clientBuilder.build().openStream().await()
    }
}