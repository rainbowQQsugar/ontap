package com.abinbev.dsa.model;

/**
 * Created by lukaszwalukiewicz on 15.01.2016.
 */

public enum UserProfile {
    brandDeveloper("Brand developer FORCE"), pe_agent("PE Agente VTA"), pe_supervisor("PE Supervisor VTA"), EC_BRAND_DEVELOPER_FORCE("EC Brand developer FORCE"), EC_SUPERVISOR_FORCE("EC Supervisor FORCE");

    private final String profileName;

    UserProfile(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }
}