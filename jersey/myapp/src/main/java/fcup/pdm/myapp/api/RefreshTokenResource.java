package fcup.pdm.myapp.api;

import fcup.pdm.myapp.dao.UserAuthDAO;
import fcup.pdm.myapp.model.TokenRequest;
import fcup.pdm.myapp.util.JwtUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/refresh")
public class RefreshTokenResource {
    private static final Logger logger = LogManager.getLogger(RegisterResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(TokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate the refresh token
        UserAuthDAO userAuthDAO = new UserAuthDAO();


        try{
            if (!userAuthDAO.isRefreshTokenValid(refreshToken)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid refresh token").build();
            }
            // Extract username from the refresh token
            String username = JwtUtil.getUsernameFromToken(refreshToken);

            // Generate a new access token
            String newAccessToken = JwtUtil.generateToken(username);

            // Optionally, generate a new refresh token
            String newRefreshToken = JwtUtil.generateRefreshToken(username);

            // Update the refresh token in the database
            userAuthDAO.updateRefreshToken(username, newRefreshToken);

            // Return the new tokens
            return Response.ok().entity("{\"accessToken\":\"" + newAccessToken + "\", \"refreshToken\":\"" + newRefreshToken + "\"}").build();
        } catch (Exception e){
            logger.warn(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error on the server when tried " +
                    "to get a new token").build();
        }

    }
}