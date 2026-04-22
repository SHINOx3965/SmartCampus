/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus;

/**
 *
 * @author SHINO
 */

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {

    // No more "SmartCampus-1.0-SNAPSHOT" in the middle! 
    // This is the direct front door to your API.
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        // CRITICAL FIX: This must match your actual package name 
        // so Jersey can find RoomResource, SensorResource, and your Mappers.
        final ResourceConfig rc = new ResourceConfig().packages("com.mycompany.smartcampus");

        // Create and start a new instance of grizzly http server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        
        System.out.println("==============================================");
        System.out.println("  Grizzly Server Started Successfully!");
        System.out.println("  Base URL : " + BASE_URI);
        System.out.println("  Discovery: GET " + BASE_URI);
        System.out.println("  Rooms    : GET " + BASE_URI + "rooms");
        System.out.println("  Sensors  : GET " + BASE_URI + "sensors");
        System.out.println("==============================================");
        System.out.println("  Press ENTER to stop the server...");

        System.in.read();
        server.shutdownNow();
    }
}