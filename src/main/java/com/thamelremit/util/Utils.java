package com.thamelremit.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.thamelremit.Vendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.thamelremit.util.Utils.class);

    private static final String SYNAPSE_HEADER = "X-Synapse-Signature";

    private static final String THAMEL_TITLE = "Thamel Remit";

    private static final String MUNCHA_TITLE = "Muncha Money";


    public static Vendor determineVendor(String json) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        String referenceNumber = (String) JsonPath.read(document, "$.extra.supp_id", new com.jayway.jsonpath.Predicate[0]);
        return referenceNumber.startsWith(Vendor.THAMELREMIT.getValue()) ? Vendor.THAMELREMIT : Vendor.MUNCHAMONEY;
    }

    public static Vendor determineVendorBySource(String source) {
        return source.equalsIgnoreCase("Thamel Remit") ? Vendor.THAMELREMIT : Vendor.MUNCHAMONEY;
    }

    public static String fetchEmail(String json) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        return (String) JsonPath.read(document, "$.logins[0].email", new com.jayway.jsonpath.Predicate[0]);
    }

    public static String determineType(String json) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        return (String) JsonPath.read(document, "$.from.type", new com.jayway.jsonpath.Predicate[0]);
    }


    public static String getWebhookEndpoint(Vendor vendor, String type) {
        String url = "";
        if (vendor == Vendor.MUNCHAMONEY) {
            url = type.equalsIgnoreCase("transaction") ? "http://localhost:6060/synapseapi/transaction/sync-status" : "http://localhost:6060/synapseapi/user/sync-document-status";
        } else if (vendor == Vendor.THAMELREMIT) {
            url = type.equalsIgnoreCase("transaction") ? "http://localhost:9090/synapseapi/transaction/sync-status" : "http://localhost:9090/synapseapi/user/sync-document-status";
        }
        return url;
    }

    public static Response triggerWebhook(Vendor vendor, String requestBody, HttpHeaders headerMap, String type) {
        Client client = ClientBuilder.newClient();
        String url = getWebhookEndpoint(vendor, type);
        LOGGER.info("Vendor : {} URL : {}", vendor.name(), url);
        WebTarget target = client.target(url);
        Invocation.Builder builder = target.request();
        List<String> headers = headerMap.getRequestHeader("X-Synapse-Signature");
        if (headers == null || headers.isEmpty()) {
            throw new InternalServerErrorException();
        }
        builder.header("X-Synapse-Signature", headers.get(0));
        return (Response) builder.post(Entity.json(requestBody), Response.class);
    }
}

