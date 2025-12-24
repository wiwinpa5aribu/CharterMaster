package com.mrh.buscharter.event;

import com.mrh.buscharter.model.TripAssignment;

/**
 * Event yang di-emit saat vehicle di-assign ke trip.
 */
public class VehicleAssignedEvent extends DomainEvent {

    private final Long assignmentId;
    private final Long tripId;
    private final Long vehicleId;
    private final String platNomor;
    private final Long driverId;
    private final String namaDriver;

    public VehicleAssignedEvent(TripAssignment assignment, Long tenantId) {
        super(tenantId);
        this.assignmentId = assignment.getId();
        this.tripId = assignment.getTrip().getId();
        this.vehicleId = assignment.getVehicle().getId();
        this.platNomor = assignment.getVehicle().getPlatNomor();
        this.driverId = assignment.getDriver() != null ? assignment.getDriver().getId() : null;
        this.namaDriver = assignment.getDriver() != null ? assignment.getDriver().getNamaLengkap() : null;
    }

    @Override
    public String getEventName() {
        return "VehicleAssigned";
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Long getTripId() {
        return tripId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public Long getDriverId() {
        return driverId;
    }

    public String getNamaDriver() {
        return namaDriver;
    }

    @Override
    public String toString() {
        return String.format("VehicleAssignedEvent{tripId=%d, vehicle=%s, driver=%s}",
            tripId, platNomor, namaDriver);
    }
}
