/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.ws.dam;

/**
 *
 * @author colin
 */
public class DamUserComment {
    public String comment = DamFieldValue.EMPTY_STRING;
    public DamUser user = new DamUser();
    public DamUserComment original = null; // if this is a reply
    public DamUserComment[] replies = new DamUserComment[0];
    public Long createdDate = 0L;
    public Long modifiedDate = 0L;
    public Integer pageNumber = 0;

    public DamUserComment() {
    }
}
