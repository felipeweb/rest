package br.com.brejaonline.services;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.message.internal.DateProvider;

@Provider
@Cached
public class CacheInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

	
	private EntityCache entityCache;
	private DateProvider dateProvider;
	
	public CacheInterceptor() {
		this.entityCache = new EntityCache();
		this.dateProvider = new DateProvider();
	}
	
	
	//Invocado na requisição 
	
	@Override
	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		
		if (requestContext.getMethod().equals("GET")) {
			String unparsedDate = requestContext
					.getHeaderString("If-Modified-Since");
			if (unparsedDate != null && !unparsedDate.trim().equals("")) {
				
				Date date = dateProvider.fromString(unparsedDate);
				String path = requestContext.getUriInfo().getPath();
				
				if (!entityCache.isUpdated(path, date)) {
					
					Response response = Response.status(Status.NOT_MODIFIED).build();
					requestContext.abortWith(response);
					return;
				}
			}
		}

	}
	
	//Invocado na resposta
	
	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {
		
		Object entity = responseContext.getEntity();
		String path = requestContext.getUriInfo().getPath();
		
		entityCache.put(path, entity);
	}

}