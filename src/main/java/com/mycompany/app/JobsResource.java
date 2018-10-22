package com.mycompany.app;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.app.models.Job;
import io.swagger.annotations.Api;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Api
@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
public class JobsResource {
    private final String template;
    private final String defaultName;
    private final Jdbi jdbi;
    private final JobsRepo jobsRepo;
    private final AtomicInteger counter;
    private final List<Job> jobs = Collections.synchronizedList(new ArrayList<>());
    final JobDao dao;

    @Inject
    public JobsResource(String template, String defaultName, Jdbi jdbi, JobsRepo jobsRepo) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicInteger();
        this.jdbi = jdbi;
        this.jobsRepo = jobsRepo;
        this.dao = jdbi.onDemand(JobDao.class);
    }

    @GET
    @Timed
    public List<Job> getAll() {
        System.out.println("------------" + jobsRepo.getName());
        return jobs;
    }

    @POST
    @Timed
    public Job save(Job job) {
        Job newJob = new Job(counter.incrementAndGet(), job.getName());
        jobs.add(newJob);
        return newJob;
    }
}