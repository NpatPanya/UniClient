package com.npat.uniclient.adapter.crosscutting;

import com.npat.uniclient.core.exception.ClientTransportException;
import com.npat.uniclient.core.model.SslConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Builds an SSL context from immutable SSL configuration without applying it to a transport.
 */
public final class SslContextFactory {
    private SslContextFactory() {
    }

    public static SSLContext from(SslConfig config) throws ClientTransportException {
        if (config == null || config.type() == SslConfig.SslType.PLATFORM_DEFAULT) {
            try {
                return SSLContext.getDefault();
            } catch (GeneralSecurityException failure) {
                throw new ClientTransportException("Unable to obtain the platform SSL context", failure);
            }
        }

        try {
            KeyManager[] keyManagers = config.keystorePath() == null
                ? null
                : keyManagers(config);
            TrustManager[] trustManagers = config.truststorePath() == null
                ? null
                : trustManagers(config);
            SSLContext context = SSLContext.getInstance(config.protocol());
            context.init(keyManagers, trustManagers, new SecureRandom());
            return context;
        } catch (GeneralSecurityException | IOException failure) {
            throw new ClientTransportException("Unable to build the configured SSL context", failure);
        }
    }

    private static KeyManager[] keyManagers(SslConfig config)
        throws GeneralSecurityException, IOException {
        KeyStore keyStore = load(config.keystorePath(), config.keystorePassword());
        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore, password(config.keystorePassword()));
        return factory.getKeyManagers();
    }

    private static TrustManager[] trustManagers(SslConfig config)
        throws GeneralSecurityException, IOException {
        KeyStore trustStore = load(config.truststorePath(), config.truststorePassword());
        TrustManagerFactory factory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        factory.init(trustStore);
        return factory.getTrustManagers();
    }

    private static KeyStore load(String path, String password)
        throws GeneralSecurityException, IOException {
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream input = new FileInputStream(path)) {
            store.load(input, password(password));
        }
        return store;
    }

    private static char[] password(String value) {
        return value == null ? null : value.toCharArray();
    }
}
