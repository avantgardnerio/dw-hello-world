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

        val jobs = BatchV1Api().listJobForAllNamespaces(null, null, null, null, null, null, null, null, null)
        for (item in jobs.items) {
            val options = V1DeleteOptions()
            // https://github.com/kubernetes-client/java/issues/86
            val status = BatchV1Api().deleteNamespacedJobAsync(
                    item.metadata.name,
                    item.metadata.namespace,
                    options, null, null, null, null, null)
            println(item.metadata.name)
        }
        val oldPods = api.listNamespacedPod("default", null, null, null, null, "job-name=hello-world-java", null, null, null, null)
        for (item in oldPods.items) {
            val options = V1DeleteOptions()
            val status = api.deleteNamespacedPodAsync(
                    item.metadata.name,
                    item.metadata.namespace,
                    options, null, null, null, null, null)
            println(item.metadata.name)
        }

        while(BatchV1Api().listJobForAllNamespaces(null, null, null, null, null, null, null, null, null).items.size > 0) {
            println("Waiting for jobs to die...")
            Thread.sleep(500);
        }

        val env = V1EnvVar()
        env.name = "foo"
        env.value = "bar"

        val container = V1Container()
        container.name = "hello-world-java"
        container.image = "maven"
        container.addEnvItem(env)
        container.command = listOf("bash", "-c", "printenv && mvn dependency:copy -Dartifact=com.github.colorgmi:hello-world-java:1.0 -DoutputDirectory=. && java -jar hello-world-java-1.0.jar")

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

        // await completion
        var res = BatchV1Api().readNamespacedJobStatus(result.metadata.name, result.metadata.namespace, null)
        while(res.status.completionTime == null) {
            println("Waiting for jobs to complete...")
            Thread.sleep(500);
            res = BatchV1Api().readNamespacedJobStatus(result.metadata.name, result.metadata.namespace, null)
        }

        val pods = api.listNamespacedPod(res.metadata.namespace, null, null, null, null, "job-name=${res.metadata.name}", null, null, null, null)
        for (item in pods.items) {
            val log = api.readNamespacedPodLog(item.metadata.name, item.metadata.namespace, null, null, null, null, null, null, null, null)
            println(log)
        }

        println("Job completed: ${res}")

    }
}