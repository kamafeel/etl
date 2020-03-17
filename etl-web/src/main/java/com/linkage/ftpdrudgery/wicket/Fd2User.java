package com.linkage.ftpdrudgery.wicket;

import org.apache.wicket.IClusterable;
import org.apache.wicket.authorization.strategies.role.Roles;

public class Fd2User implements IClusterable {
	private static final long serialVersionUID = -4989739308752426349L;
	private final String uid;
	private final Roles roles;

	/**
	 * Construct.
	 * 
	 * @param uid
	 *            the unique user id
	 * @param roles
	 *            a comma seperated list of roles (e.g. USER,ADMIN)
	 */
	public Fd2User(String uid, String roles) {
		if (uid == null) {
			throw new IllegalArgumentException("uid must be not null");
		}
		if (roles == null) {
			throw new IllegalArgumentException("roles must be not null");
		}
		this.uid = uid;
		this.roles = new Roles(roles);
	}

	/**
	 * Whether this user has the given role.
	 * 
	 * @param role
	 * @return whether this user has the given role
	 */
	public boolean hasRole(String role) {
		return this.roles.hasRole(role);
	}

	/**
	 * Whether this user has any of the given roles.
	 * 
	 * @param roles
	 *            set of roles
	 * @return whether this user has any of the given roles
	 */
	public boolean hasAnyRole(Roles roles) {
		return this.roles.hasAnyRole(roles);
	}

	/**
	 * Gets the uid.
	 * 
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return uid + "[" + roles + "]";
	}
}
