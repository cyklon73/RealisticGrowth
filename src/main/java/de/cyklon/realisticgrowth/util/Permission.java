package de.cyklon.realisticgrowth.util;

public final class Permission extends org.bukkit.permissions.Permission {

	public static final Permission UPDATE = perm("update");

	private Permission(String name) {
		super(name);
	}

	private static Permission perm(String name) {
		return new Permission("rg." + name);
	}

}
