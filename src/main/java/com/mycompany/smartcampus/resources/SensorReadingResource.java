/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

import com.mycompany.smartcampus.DataStore.Store;
import com.mycompany.smartcampus.Exception.SensorUnavailableException;
import com.mycompany.smartcampus.model.Sensor;
import com.mycompany.smartcampus.model.SensorReading;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author SHINO
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        Sensor sensor = Store.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor '" + sensorId + "' does not exist.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Use CopyOnWriteArrayList for thread safety
        List<SensorReading> readings = Store.sensorReadings
                .getOrDefault(sensorId, new CopyOnWriteArrayList<>());

        return Response.ok(readings).build();
    }
    
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = Store.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor '" + sensorId + "' does not exist.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
        throw new SensorUnavailableException(
            "Sensor '" + sensorId + "' is currently under maintenance and cannot accept readings.");
    }
        
            Store.sensorReadings
                .computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>())
                .add(reading);

        // 3. Update the parent sensor's currentValue to this newest reading
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
        
    }
}