/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author colin
 */
public class DamUser {
    private String username = DamFieldValue.EMPTY_STRING;
    private String firstName = DamFieldValue.EMPTY_STRING;
    private String lastName = DamFieldValue.EMPTY_STRING;
    private String email = DamFieldValue.EMPTY_STRING;
    private String[] roles = new String[0];

    public DamUser() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
