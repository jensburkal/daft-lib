/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.noosh;

/**
 *
 * @author colin
 */
public class NooshAdmin {
    private String server = null;
    private String domain = null;
    private String identity = null;
    private String sharedPassword = null;

    public NooshAdmin() {

    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getSharedPassword() {
        return sharedPassword;
    }

    public void setSharedPassword(String sharedPassword) {
        this.sharedPassword = sharedPassword;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

}
