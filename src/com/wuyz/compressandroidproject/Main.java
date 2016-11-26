package com.wuyz.compressandroidproject;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    private static int count = 0;
    private static final String[] blackNames = new String[] {"bin", "build", "gen", ".git", ".svn"};

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("input error! usage: compressandroidproject srcDir");
            return;
        }
        File srcPath = new File(args[0]);
        if (!srcPath.exists()) {
            System.err.println("file or path not exist: " + args[0]);
            return;
        }
        long startTime = System.currentTimeMillis();
        count = 0;
        String zipName = srcPath.getAbsolutePath() + "-" + System.currentTimeMillis() + ".zip";
        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipName, false))) {
            if (srcPath.isFile())
                zipFile(outputStream, srcPath, "");
            else {
                File[] files = srcPath.listFiles();
                for (File f : files) {
                    boolean isBlack = false;
                    final String name = f.getName();
                    for (String s : blackNames) {
                        if (s.equals(name)) {
                            isBlack = true;
                            break;
                        }
                    }
                    if (isBlack)
                        continue;
                    zipFile(outputStream, f, "");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long costTime = System.currentTimeMillis() - startTime;
        System.out.println("total compress " + count + " files cost " + costTime / 1000 + "s");
    }

    public static boolean unZip(String zipPath, String destPath) {
        System.out.println("unZip " + zipPath + " to " + destPath);
        if (zipPath == null || zipPath.isEmpty() || destPath == null || destPath.isEmpty()) {
            return false;
        }
        File srcFile = new File(zipPath);
        if (!srcFile.exists() || !srcFile.isFile())
            return false;
        File destFile = new File(destPath);
        if (!destFile.exists()) {
            if (!destFile.mkdirs()) {
                System.err.println("mkdir error: " + destFile);
                return false;
            }
        }
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(zipPath));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
//                Log2.d(TAG, "zip: %s", zipEntry);
                File dest = new File(destFile, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    dest.mkdirs();
                } else {
                    File parent = dest.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    FileOutputStream outputStream = new FileOutputStream(dest);
                    byte[] buffer = new byte[2048];
                    int n;
                    while ((n = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, n);
                    }
                    outputStream.close();
                }

            }
            zipInputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean zipFile(ZipOutputStream outputStream, File file, String parent) {
        if (!file.exists()) {
            return false;
        }
        FileInputStream inputStream = null;
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                parent = parent.isEmpty() ? file.getName() : (parent + File.separator + file.getName());
                ZipEntry zipEntry = new ZipEntry(parent + File.separator);
                outputStream.putNextEntry(zipEntry);
                outputStream.closeEntry();
                for (File f : files) {
                    boolean isBlack = false;
                    final String name = f.getName();
                    for (String s : blackNames) {
                        if (s.equals(name)) {
                            isBlack = true;
                            break;
                        }
                    }
                    if (isBlack)
                        continue;
                    zipFile(outputStream, f, parent);
                }
            } else if (file.isFile()) {
                System.out.println(file.getAbsolutePath());
                ZipEntry zipEntry;
                if (!parent.isEmpty()) {
                    zipEntry = new ZipEntry(parent + File.separator + file.getName());
                } else {
                    zipEntry = new ZipEntry(file.getName());
                }
                outputStream.putNextEntry(zipEntry);
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int n;
                while ((n = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, n);
                }
//                outputStream.closeEntry();
                inputStream.close();
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
