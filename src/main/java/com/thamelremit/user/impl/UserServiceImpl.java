package com.thamelremit.user.impl;

import com.thamelremit.dto.User;
import com.thamelremit.user.UserService;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

public class UserServiceImpl implements UserService {
    public String fetchUserVendor(String emailAddress) {
        Client client = ClientBuilder.newClient();
        String url = "http://localhost:7070/duplicate_users?email_address=" + emailAddress;
        WebTarget target = client.target(url);
        Invocation.Builder builder = target.request();
        Response response = (Response) builder.get(Response.class);
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            List<User> users = (List<User>) response.readEntity(new GenericType<List<User>>() {});

            if (users.isEmpty()) {
                return "";
            }
            return ((User) users.get(0)).getService_providers_name();
        }
        throw new InternalServerErrorException();
    }
}