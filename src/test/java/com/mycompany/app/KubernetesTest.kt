package com.mycompany.app

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
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
    }
}