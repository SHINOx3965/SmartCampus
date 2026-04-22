/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author SHINO
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();

        // API metadata
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors");
        response.put("contact", "admin@smartcampus.westminster.ac.uk");

        // Hypermedia links — clients can use these to navigate the API
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    "/api/v1/");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("resources", links);

        return Response.ok(response).build();
    }
}
