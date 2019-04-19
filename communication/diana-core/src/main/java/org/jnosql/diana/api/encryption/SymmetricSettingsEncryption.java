/*
 *
 *  Copyright (c) 2019 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.jnosql.diana.api.encryption;

import org.jnosql.diana.api.JNoSQLException;
import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.SettingsEncryption;
import org.jnosql.diana.api.SettingsPriority;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Symmetric-Key Cryptography is an encryption system in which the same key is used for the encoding and
 * decoding of the data. The safe distribution of the key is one of the drawbacks of this method,
 * but what it lacks in security it gains in time complexity.
 *The SettingsEncryption has two properties configurations.
 *
 * {@link SymmetricSettingsEncryption#KEY_PROPERTY} The mandatory configuration that defines the password to both encrypt and decrypt the property.
 * {@link SymmetricSettingsEncryption#CRYPT_PROPERTY} This property defines the crypt algorithm that will use on the symmetric encryption process. The default value is DESede.
 * To know more about: https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html
 *
 */
public class SymmetricSettingsEncryption implements SettingsEncryption {

    /**
     * The key property
     */
    public static final String KEY_PROPERTY = "jakarta.nosql.encryption.symmetric.key";
    public static final String CRYPT_PROPERTY = "jakarta.nosql.encryption.symmetric.crypt";

    private static final String MISSING_KEY_MESSAGE = "To use 3DES encryption you need to set the key using the property;" +
            KEY_PROPERTY;
    private static final int MIN_VALUE = 24;
    private static final String ALGORITHM = "md5";
    private static final String CRYPT_DEFAULT_ALGORITHM = "DESede";

    @Override
    public String encrypt(String property, Settings settings) {
        requireNonNull(property, "property is required");
        requireNonNull(settings, "settings is required");

        try {
            Cipher cipher = getCipher(settings, Cipher.ENCRYPT_MODE);

            byte[] plainTextBytes = property.getBytes(UTF_8);
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.getEncoder().encode(buf);
            return new String(base64Bytes, UTF_8);
        } catch (Exception ex) {
            throw new EncryptionException("An error when try to encrypt a property using 3DES", ex);
        }
    }

    @Override
    public String decrypt(String property, Settings settings) {
        requireNonNull(property, "property is required");
        requireNonNull(settings, "settings is required");
        try {
            byte[] message = Base64.getDecoder().decode(property.getBytes(UTF_8));
            Cipher decipher = getCipher(settings, Cipher.DECRYPT_MODE);
            byte[] plainText = decipher.doFinal(message);
            return new String(plainText, UTF_8);
        } catch (Exception ex) {
            throw new EncryptionException("An error when try to decrypt a property using 3DES", ex);
        }
    }

    private Cipher getCipher(Settings settings, int mode)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        String key = getKey(settings);
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        byte[] digestOfPassword = md.digest(key.getBytes(UTF_8));
        byte[] keyBytes = Arrays.copyOf(digestOfPassword, MIN_VALUE);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }
        String crypt = SettingsPriority.get(CRYPT_PROPERTY, settings)
                .map(Object::toString)
                .orElse(CRYPT_DEFAULT_ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(keyBytes, crypt);
        Cipher cipher = Cipher.getInstance(crypt);
        cipher.init(mode, secretKey);
        return cipher;
    }

    private String getKey(Settings settings) {
        return SettingsPriority.get(KEY_PROPERTY, settings)
                .map(Object::toString)
                .orElseThrow(() -> new JNoSQLException(MISSING_KEY_MESSAGE));
    }
}