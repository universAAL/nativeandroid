/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware.android.modules.impl;

import java.io.File;
import java.io.FileOutputStream;

import android.util.Log;

/**
 * A simple class for handling common file operation
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision: 2652 $ ($LastChangedDate: 2013-04-02 14:36:09
 *          +0200 (Tue, 02 Apr 2013) $)
 *
 */
// TODO adapt to android!!
public class AndroidFileUtils {
	
	private static final String TAG = "AndroidFileUtils";

    public static File createFileFromByte( byte[] content,
            String dst) {
        return createFileFromByte( content, dst, false);
    }

    public static File createFileFromByte(/*ModuleContext mc,*/ byte[] content,
            String dst, boolean overwrite) {
        File file = new File(dst);
        File parent = file.getParentFile();
        if (file.exists() == true && file.isDirectory() == true) {
            Log.e(TAG, "Error while creating file the destination "
                            + file.getPath() + " exists but it is a directory" );
            return null;
        }
        if (file.exists() && overwrite == true && file.delete() == false) {
            Log.e(TAG, "Error while creating file the destination "
                            + file.getPath() + " exists but couldn't delete it" );
            return null;
        } else if (file.exists() == true && overwrite == false) {
            Log.e(TAG, "Error while creating file the destination "
                            + file.getPath() + " exists");
            return null;
        }
        if (parent == null) {
            Log.d(TAG, "The file is considered as relative path, so the file will created in "
                            + new File(".").getAbsolutePath());
        } else {
            if (parent.exists() == true && parent.isDirectory() == false) {
                Log.e(TAG, "Error while creating file the destination folder "
                                + parent.getPath() + " exists but it is a file" );
                return null;
            }
            if (parent.exists() == true && parent.canWrite() == false) {
                Log.e(TAG, "Error while creating file the destination folder "
                                + parent.getPath()
                                + " exists but we don't have permission to write" );
                return null;
            }
            if (parent.exists() == false && parent.mkdirs() == false) {
                Log.e(TAG, "Error while creating file the destination folder "
                                + parent.getPath()
                                + " does not exist and the creation of folder failed");
                return null;
            }
        }
        try {
            file.createNewFile();
            FileOutputStream fos;
            fos = new FileOutputStream(file);
            fos.write(content);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            Log.e(TAG, "Error while creating file "
                            + file.getPath() );
            return null;
        }
        return file;
    }
}
