/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

import com.mycompany.smartcampus.DataStore.Store;
import com.mycompany.smartcampus.Exception.LinkedResourceNotFoundException;
import com.mycompany.smartcampus.model.Sensor;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

 
    // GET /sensors — list all sensors
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(Store.sensors.values());

        // Apply filter if the 'type' query param was provided
        if (type != null && !type.trim().isEmpty()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(result).build();
    }
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Sensor 'id' is required.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (Store.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A sensor with id '" + sensor.getId() + "' already exists.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validate that the referenced room actually exists
        if (sensor.getRoomId() == null || !Store.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Cannot create sensor: room '" + sensor.getRoomId() +
                    "' does not exist. Please create the room first.");
        }
        
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

      

        // Save the sensor and link it to the room
        Store.sensors.put(sensor.getId(), sensor);
        Store.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise an empty thread-safe readings list for this sensor
        Store.sensorReadings.put(sensor.getId(), new CopyOnWriteArrayList<>());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }
    @GET
    @Path("{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = Store.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor '" + sensorId + "' does not exist.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(sensor).build();
    }
    
    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}