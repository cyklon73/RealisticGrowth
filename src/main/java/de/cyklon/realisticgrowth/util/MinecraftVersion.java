package de.cyklon.realisticgrowth.util;

public class MinecraftVersion {

	private final int baseVersion;
	private final int bigVersion;
	private final int smallVersion;


	public MinecraftVersion(int baseVersion, int bigVersion, int smallVersion) {
		this.baseVersion = baseVersion;
		this.bigVersion = bigVersion;
		this.smallVersion = smallVersion;
	}

	public int getBaseVersion() {
		return baseVersion;
	}

	public int getBigVersion() {
		return bigVersion;
	}

	public int getSmallVersion() {
		return smallVersion;
	}

	public int getVersion() {
		return baseVersion*100 + bigVersion*10 + smallVersion;
	}

	/**
	 * @return true if the given version ist equal or higher
	 */
	public boolean checkVersion(int baseVersion, int bigVersion, int smallVersion) {
		return checkVersion(baseVersion*100 + bigVersion*10 + smallVersion);
	}

	/**
	 * @return true if the given version ist equal or higher
	 */
	public boolean checkVersion(MinecraftVersion version) {
		return checkVersion(version.getVersion());
	}

	/**
	 * @return true if the given version ist equal or higher
	 */
	public boolean checkVersion(int version) {
		return getVersion() >= version;
	}

	public String formatted() {
		return baseVersion + "." + bigVersion + "." + smallVersion;
	}

	@Override
	public String toString() {
		return formatted();
	}
}
