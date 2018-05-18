package com.emclien.emclientlib.interfaces;

/**
 * data sync listener
 */
public interface DataSyncListener {
    /**
     * sync complete
     *
     * @param success true：data sync successful，false: failed to sync data
     */
    void onSyncComplete(boolean success);
}