package com.tts.app.configcenter.service.resource;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.tts.app.configcenter.model.util.QueryFilter;
import com.tts.lib.model.generic.DataModel;
import com.tts.lib.model.generic.GenericDao;

public abstract class PersistenceResourceImpl<T extends DataModel, QUERY extends QueryFilter, DAO extends GenericDao<T, QUERY>> implements PersistenceResource<T, QUERY> {

    public abstract DAO getDao();
    
    @Context
    UriInfo uri;
    
    @GET
    @Path("/{id}")
    @Override
    public Response getObject(@PathParam("id") Long id) {
        T item = getDao().get(id);
        return item == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(item).build();
    }

    @POST
    @Override
    public Response addObject(T obj) {
        obj = getDao().add(obj);
        URI taskURI = uri.getRequestUriBuilder().path(getClass(), "getObject").build(obj.getId());
        return Response.created(taskURI).build();
    }
    
    @POST
    @Override
    @Path("/query")
    public List<T> findByQuery(QUERY filter) {
        return getDao().findByQuery(filter);
    }

    @GET
    @Override
    public Collection<T> getObjects() {
        return getDao().gets();
    }

    @PUT
    @Path("/{id}")
    @Override
    public void updateObject(@PathParam("id") Long id, T object) {
        object.setId(id);
        getDao().update(object);
    }
    
    @DELETE
    @Path("/{id}")
    @Override
    public void deleteObject(@PathParam("id") Long id) {
        getDao().delete(id);
    }

}
