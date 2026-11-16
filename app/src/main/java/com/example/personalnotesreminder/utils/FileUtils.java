package com.example.personalnotesreminder.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static String copyFileToInternalStorage(Context context, Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            if (fileName == null || fileName.isEmpty()) {
                fileName = "doc_" + System.currentTimeMillis();
            }

            File dir = new File(context.getFilesDir(), "study_documents");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(dir, System.currentTimeMillis() + "_" + fileName);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int length;
            if (inputStream != null) {
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static void openFile(Context context, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(context, "File path is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist on device", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );

            String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            if (mimeType == null) {
                mimeType = "*/*";
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(intent, "Open file with");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
        File file = new File(filePath);
        return file.exists() && file.delete();
    }
}
