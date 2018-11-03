package com.mycompany.app

import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.BatchV1Api
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*
import io.kubernetes.client.proto.V1
import io.kubernetes.client.proto.V1Batch
import io.kubernetes.client.util.Config;
import org.junit.Test


class KubernetesTest {
    @Test
    fun `it should list pods`() {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)

        val api = CoreV1Api()
        val list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null)
        for (item in list.items) {
            println(item.metadata.name)
        }

        val nodes = api.listNode(null, null, null, null, null, null, null, null, null)
        for (item in nodes.items) {
            println(item.metadata.name)
        }

        val container = V1Container()
        container.name = "hello-world-java"
        container.image = "maven"
        container.command = listOf("bash", "-c", "mvn dependency:copy -Dartifact=com.github.colorgmi:hello-world-java:1.0 -DoutputDirectory=. && java -jar hello-world-java-1.0.jar")

        val podSpec = V1PodSpec()
        podSpec.containers = listOf(container)
        podSpec.restartPolicy = "Never"

        val template = V1PodTemplateSpec()
        template.spec = podSpec

        val jobSpec = V1JobSpec()
        jobSpec.template = template
        jobSpec.backoffLimit = 4

        val metaData = V1ObjectMeta()
        metaData.name = "hello-world-java"

        val job = V1Job()
        job.metadata = metaData
        job.spec = jobSpec

        val result = BatchV1Api().createNamespacedJob("default", job, null)
        println(result)
    }
}