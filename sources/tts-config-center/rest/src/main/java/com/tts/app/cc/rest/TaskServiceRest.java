package com.tts.app.cc.rest;

import java.net.URI;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ops4j.pax.cdi.api.OsgiService;

import com.tts.app.cc.model.Task;
import com.tts.app.cc.model.TaskService;

@Path("/task")
@Named
@Consumes({ "application/json", "application/xml" })
@Produces({ "application/json", "application/xml" })
public class TaskServiceRest {

    @OsgiService
    @Inject
    TaskService taskService;

    @Context
    UriInfo uri;

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") Integer id) {
        Task task = taskService.find(id);
        return task == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(task).build();
    }

    @POST
    public Response add(Task task) {
        taskService.add(task);
        URI taskURI = uri.getRequestUriBuilder().path(TaskServiceRest.class, "getTask").build(task.getId());
        return Response.created(taskURI).build();
    }

    @GET
    public Collection<Task> getTasks() {
        return taskService.find();
    }

    @PUT
    @Path("{id}")
    public void update(@PathParam("id") Integer id, Task task) {
        task.setId(id);
        taskService.update(task);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") Integer id) {
        taskService.delete(id);
    }

}