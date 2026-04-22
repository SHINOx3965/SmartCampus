/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

import com.mycompany.smartcampus.DataStore.Store;
import com.mycompany.smartcampus.Exception.RoomNotEmptyException;
import com.mycompany.smartcampus.model.Room;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;


@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {


    // GET /rooms — returns the full list of room objects
    @GET
    public Response getAllRooms() {
        Collection<Room> allRooms = Store.rooms.values();
        return Response.ok(new ArrayList<>(allRooms)).build();
    }


    // POST /rooms — create a new room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Room 'id' is required.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (Store.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A room with id '" + room.getId() + "' already exists.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Ensure sensorIds list starts empty — clients cannot pre-assign sensors here
        room.setSensorIds(new ArrayList<>());
        Store.rooms.put(room.getId(), room);

        return Response.status(Response.Status.CREATED).entity(room).build();
    }
    
    // GET /rooms/{roomId} — fetch a single room by its ID
    @GET
    @Path("{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = Store.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Room '" + roomId + "' does not exist.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(room).build();
    }


    // DELETE /rooms/{roomId} — remove a room
    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = Store.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Room '" + roomId + "' does not exist.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Safety check — block deletion if the room still has sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because it still has " +
                    room.getSensorIds().size() + " sensor(s) assigned to it. " +
                    "Please remove all sensors first.");
        }

        Store.rooms.remove(roomId);
        
        return Response.ok("{\"status\": \"Deleted\", \"message\": \"Room '" + roomId + "' was successfully deleted.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}