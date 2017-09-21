package com.obs;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyStoreTest {

    private final String storeType = "jceks";

    @Test
    public void loadJks() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore jks = KeyStore.getInstance(storeType);
        String keystoreFilename = "jwt.jks";
        String keystorePassword = "P@ssw0rd";

        jks.load(new FileInputStream(new ClassPathResource(keystoreFilename).getFile()), keystorePassword.toCharArray());
        assertThat(Collections.list(jks.aliases())).containsExactly("jwt");

        Key key = jks.getKey("jwt", keystorePassword.toCharArray());
        assertThat(key).isInstanceOf(PrivateKey.class);
        Certificate cert = jks.getCertificate("jwt");
        PublicKey publicKey = cert.getPublicKey();
        assertThat(publicKey).hasFieldOrPropertyWithValue("algorithm", "RSA")
                .hasFieldOrPropertyWithValue("format", "X.509");
    }
}
