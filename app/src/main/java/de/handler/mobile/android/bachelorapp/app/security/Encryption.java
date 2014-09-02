package de.handler.mobile.android.bachelorapp.app.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Handles encryption of strings
 */
public class Encryption {

    public String encryptPassword(final String password) {
        try {

            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
