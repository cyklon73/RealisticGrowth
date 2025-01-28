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

	/**
	 * @return true if the current version ist equal or higher than the given
	 */
	public boolean checkVersion(int baseVersion, int bigVersion, int smallVersion) {
		return compareVersions(formatted(), "%s.%s.%s".formatted(baseVersion, bigVersion, smallVersion)) >= 0;
	}

	/**
	 * @return true if the current version ist equal or higher than the given
	 */
	public boolean checkVersion(MinecraftVersion version) {
		return compareVersions(formatted(), version.formatted()) >= 0;
	}

	public String formatted() {
		return baseVersion + "." + bigVersion + "." + smallVersion;
	}

	@Override
	public String toString() {
		return formatted();
	}

	public static MinecraftVersion parseVersion(String version) {
		version = version.substring(0, version.indexOf('-'));
		String[] versions = version.split("\\.");
		int baseVersion = Integer.parseInt(versions[0]);
		int bigVersion = Integer.parseInt(versions[1]);
		int smallVersion = versions.length < 3 ? 0 : Integer.parseInt(versions[2]);
		return new MinecraftVersion(baseVersion, bigVersion, smallVersion);
	}

	public static int compareVersions(String version1, String version2) {
		int comparisonResult = 0;

		String[] version1Splits = version1.split("\\.");
		String[] version2Splits = version2.split("\\.");
		int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

		for (int i = 0; i < maxLengthOfVersionSplits; i++){
			Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
			Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
			int compare = v1.compareTo(v2);
			if (compare != 0) {
				comparisonResult = compare;
				break;
			}
		}
		return comparisonResult;
	}
}
