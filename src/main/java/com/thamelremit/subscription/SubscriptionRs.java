package com.thamelremit.subscription;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.thamelremit.Vendor;
import com.thamelremit.user.impl.UserServiceImpl;
import com.thamelremit.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/subscription")
@Produces({"application/json"})
public class SubscriptionRs {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.thamelremit.subscription.SubscriptionRs.class);

    @POST
    @Path("/transaction")
    public Response webhookTransaction(@Context HttpHeaders headers, String requestBody) {
        LOGGER.info("#webhookTransaction. Payload Details : {}", requestBody);
        String type = Utils.determineType(requestBody);
        if ("IOU".equalsIgnoreCase(type)) {
            return Response.status(Response.Status.OK).build();
        }
        Vendor vendor = Utils.determineVendor(requestBody);
        try {
            Response response = Utils.triggerWebhook(vendor, requestBody, headers, "transaction");
            return Response.status(response.getStatus()).build();
        } catch (Exception e) {
            LOGGER.error("Exception on calling webhook : {}" + e);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/user")
    public Response webhookUser(@Context HttpHeaders headers, String requestBody) {
        LOGGER.info("#webhookUser. Payload Details : {} ", requestBody);
        String emailAddress = Utils.fetchEmail(requestBody);
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        String resultantVendor = userServiceImpl.fetchUserVendor(emailAddress);
        if ("".equals(resultantVendor)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        Vendor vendor = Utils.determineVendorBySource(resultantVendor);
        try {
            Response response = Utils.triggerWebhook(vendor, requestBody, headers, "user");
            return Response.status(response.getStatus()).build();
        } catch (Exception e) {
            LOGGER.error("Exception on calling webhook : {}" + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
