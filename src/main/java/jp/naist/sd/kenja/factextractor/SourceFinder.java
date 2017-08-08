package jp.naist.sd.kenja.factextractor;

import java.io.File;

public class SourceFinder {

    private static SourceFinder instance;

    private String[] paths;

    private SourceFinder() {
    }

    public static void initialize(String[] paths) {
        instance = new SourceFinder();
        instance.paths = paths;
    }

    public static SourceFinder getInstance() {
        return instance;
    }

    public String findSource(String qualifiedName) {
        int dollar = qualifiedName.indexOf("$");
        if (dollar != -1) {
            qualifiedName = qualifiedName.substring(0, dollar);
        }
        qualifiedName = qualifiedName.replace(".", "/") + ".java";
        for (String path : paths) {
            if (!path.endsWith("/")) {
                path += "/";
            }
            File f = new File(path + qualifiedName);
            if (f.exists() && !f.isDirectory()) {
                return (path + qualifiedName).replace("_", "__").replace("/", "_");
            }
        }
        return null;
    }

}