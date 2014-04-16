package com.apigee.cs.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    static Logger log = LoggerFactory.getLogger(ZipUtils.class);


    public void unzipArchive(File archive, File outputDir) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
        } catch (Exception e) {
            log.error("Error while extracting file " + archive, e);
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        log.debug("Extracting: " + entry);
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        log.debug("Creating dir " + dir.getName());
        dir.mkdirs();

    }

    public void zipDir(File zipFileName, File dirObj, String prefix) throws IOException {

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        log.debug("Creating : " + zipFileName);
        addDir(dirObj, out, dirObj, prefix);
        out.close();
    }

    static void addDir(File dirObj, ZipOutputStream out, File root, String prefix) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], out, root, prefix);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            log.debug(" Adding: " + files[i].getAbsolutePath());

            String relativePath = files[i].getCanonicalPath().substring(root.getCanonicalPath().length());
            while (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            while (relativePath.startsWith("\\")) {
                relativePath = relativePath.substring(1);
            }

            String left = Matcher.quoteReplacement("\\");
            String right = Matcher.quoteReplacement("/");

            relativePath = relativePath.replaceAll(left, right);
            relativePath = (prefix == null) ? relativePath : prefix + "/" + relativePath;
            out.putNextEntry(new ZipEntry(relativePath));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }

}