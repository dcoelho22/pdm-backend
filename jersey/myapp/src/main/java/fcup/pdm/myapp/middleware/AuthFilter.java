package fcup.pdm.myapp.middleware;

import fcup.pdm.myapp.dao.UserAuthDAO;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.net.URI;

@Provider
public class AuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        URI requestUri = requestContext.getUriInfo().getRequestUri();
        String path = requestUri.getPath();

        if (path.endsWith("/login") || path.endsWith("/register") || path.endsWith("/refresh")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Authorization header must be provided").build());
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        try {
            // Validate the refresh token
            UserAuthDAO userAuthDAO = new UserAuthDAO();
            userAuthDAO.isRefreshTokenValid(token);
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build());
        }
    }
}
