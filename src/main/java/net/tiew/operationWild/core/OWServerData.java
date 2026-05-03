// Nouveau fichier : OWServerData.java
package net.tiew.operationWild.core;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OWServerData {
    public static final ConcurrentHashMap<UUID, AtomicInteger> tigerKillCounts = new ConcurrentHashMap<>();
}