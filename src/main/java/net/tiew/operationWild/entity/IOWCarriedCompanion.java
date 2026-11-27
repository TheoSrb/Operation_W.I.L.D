package net.tiew.operationWild.entity;

public interface IOWCarriedCompanion {

    boolean isCarried();

    void dismountFromCarrier();

    default int carrySlot() {
        return 0;
    }
}
