package com.gruposantoro.sedeso.tools;

import android.content.pm.PackageManager;

/**
 * Created by ULISES on 11/08/2016.
 */
public abstract class PermissionUtil {

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
