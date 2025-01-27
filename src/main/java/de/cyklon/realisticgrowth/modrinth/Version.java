package de.cyklon.realisticgrowth.modrinth;

import lombok.Data;

@Data
public class Version {

    private final String name;
    private final String version_number;
    private final String changelog;
    private final Dependency[] dependencies;
    private final String[] game_versions;
    private final String version_type;
    private final String[] loaders;
    private final boolean featured;
    private final String status;
    private final String id;
    private final String project_id;
    private final String author_id;
    private final String date_published;
    private final int downloads;
    private final String changelog_url;
    private final File[] files;


    @Data
    public static class Dependency {
        private final String version_id;
        private final String project_id;
        private final String file_name;
        private final String dependency_type;
    }

    @Data
    public static class File {
        private final Hash hashes;
        private final String url;
        private final String filename;
        private final boolean primary;
        private final int size;
        private final String file_type;
    }

    @Data
    public static class Hash {
        private final String sha512;
        private final String sha1;
    }

}
