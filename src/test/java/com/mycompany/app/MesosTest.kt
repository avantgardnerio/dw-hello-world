package com.mycompany.app

import com.mesosphere.mesos.rx.java.SinkOperation
import com.mesosphere.mesos.rx.java.SinkOperations
import com.mesosphere.mesos.rx.java.protobuf.ProtobufMesosClientBuilder
import com.mesosphere.mesos.rx.java.protobuf.SchedulerCalls
import com.mesosphere.mesos.rx.java.util.UserAgentEntry
import org.apache.commons.io.FileUtils
import org.apache.mesos.v1.Protos
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class MesosTest {

    var master: Process? = null
    var slave: Process? = null

    @Before
    fun setup() {
        FileUtils.deleteDirectory(File("/tmp/mesos"))
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
        val frameworkId = Protos.FrameworkID.newBuilder().setValue(fwId).build()

        val clientBuilder = ProtobufMesosClientBuilder.schedulerUsingProtos()
                .mesosUri(mesosUri)
                .applicationUserAgentEntry { UserAgentEntry("my-app", "1.0") }

        val fwInfo = Protos.FrameworkInfo.newBuilder()
                .setId(frameworkId)
                .setUser(Optional.ofNullable(System.getenv("user")).orElse("root")) // https://issues.apache.org/jira/browse/MESOS-3747
                .setName("myApp")
                .setFailoverTimeout(0.0)
                .setRole(role)
                .build()
        val subscribeCall = SchedulerCalls.subscribe(frameworkId, fwInfo)

        clientBuilder
                .subscribe(subscribeCall)
                .processStream { unicastEvents ->
                    val events = unicastEvents.share()
                    val acceptor = events
                            .filter { it.getType() == org.apache.mesos.v1.scheduler.Protos.Event.Type.OFFERS }
                            .flatMap { rx.Observable.from(it.getOffers().getOffersList()) }
                            .map { offer ->
                                //println(offer)
                                val agentId = offer.agentId
                                val taskId = UUID.randomUUID().toString()
                                val task = sleepTask(agentId, taskId, role, 0.5, role, 16.0)
                                val call = sleep(frameworkId, listOf(offer.id), listOf(task))
                                println("Scheduling $taskId...")
                                SinkOperations.sink(call, { println("Completed $taskId") }, { err -> println(err) })
                            }
                            .map { Optional.of(it) }
                    val logger = events
                            .doOnNext { e -> println(e) }
                            .map { e -> Optional.empty<SinkOperation<org.apache.mesos.v1.scheduler.Protos.Call>>() }
                    acceptor.mergeWith(logger)
                }

        clientBuilder.build().openStream().await()
    }

    private fun sleep(
            frameworkId: Protos.FrameworkID,
            offerIds: List<Protos.OfferID>,
            tasks: List<Protos.TaskInfo>
    ): org.apache.mesos.v1.scheduler.Protos.Call {
        return org.apache.mesos.v1.scheduler.Protos.Call.newBuilder()
                .setFrameworkId(frameworkId)
                .setType(org.apache.mesos.v1.scheduler.Protos.Call.Type.ACCEPT)
                .setAccept(
                        org.apache.mesos.v1.scheduler.Protos.Call.Accept.newBuilder()
                                .addAllOfferIds(offerIds)
                                .addOperations(
                                        Protos.Offer.Operation.newBuilder()
                                                .setType(Protos.Offer.Operation.Type.LAUNCH)
                                                .setLaunch(
                                                        Protos.Offer.Operation.Launch.newBuilder()
                                                                .addAllTaskInfos(tasks)
                                                )
                                )
                )
                .build()
    }

    private fun sleepTask(
            agentId: Protos.AgentID,
            taskId: String,
            cpusRole: String,
            cpus: Double,
            memRole: String,
            mem: Double
    ): Protos.TaskInfo {
        val sleepSeconds = Optional.ofNullable(System.getenv("SLEEP_SECONDS")).orElse("1")
        return Protos.TaskInfo.newBuilder()
                .setName(taskId)
                .setTaskId(
                        Protos.TaskID.newBuilder()
                                .setValue(taskId)
                )
                .setAgentId(agentId)
                .setCommand(
                        Protos.CommandInfo.newBuilder()
                                .setEnvironment(Protos.Environment.newBuilder()
                                        .addVariables(
                                                Protos.Environment.Variable.newBuilder()
                                                        .setName("SLEEP_SECONDS").setValue(sleepSeconds)
                                        ))
                                .setValue("env | sort && sleep \$SLEEP_SECONDS")
                )
                .addResources(Protos.Resource.newBuilder()
                        .setName("cpus")
                        .setRole(cpusRole)
                        .setType(Protos.Value.Type.SCALAR)
                        .setScalar(Protos.Value.Scalar.newBuilder().setValue(cpus)))
                .addResources(Protos.Resource.newBuilder()
                        .setName("mem")
                        .setRole(memRole)
                        .setType(Protos.Value.Type.SCALAR)
                        .setScalar(Protos.Value.Scalar.newBuilder().setValue(mem)))
                .build()
    }


}