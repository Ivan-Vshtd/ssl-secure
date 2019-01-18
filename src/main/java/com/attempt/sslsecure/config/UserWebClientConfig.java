package com.attempt.sslsecure.config;


import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iveshtard
 * @since 1/15/2019
 *
 * this config allows webClient works with mutual ssl authentication
 */
@Configuration
public class UserWebClientConfig {

    private static final String BASEURL = "https://localhost:8443";

    @Value("${server.ssl.trust-store}")
    String trustStorePath;
    @Value("${server.ssl.trust-store-password}")
    String trustStorePasswd;
    @Value("${server.ssl.key-store}")
    String keyStorePath;
    @Value("${server.ssl.key-store-password}")
    String keyStorePasswd;
    @Value("${server.ssl.key-alias}")
    String keyAlias;

    @Bean
    public WebClient createWebClient() {
        SslContext sslContext;
        try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(new FileInputStream(ResourceUtils.getFile(trustStorePath)), trustStorePasswd.toCharArray());

                List<Certificate> certificateCollection = Collections.list(trustStore.aliases()).stream().filter(t -> {
                    try {
                        return trustStore.isCertificateEntry(t);
                    } catch (KeyStoreException e1) {
                        throw new RuntimeException("Error reading truststore", e1);
                    }
                }).map(t -> {
                    try {
                        return trustStore.getCertificate(t);
                    } catch (KeyStoreException e2) {
                        throw new RuntimeException("Error reading truststore", e2);
                    }
                }).collect(Collectors.toList());

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), keyStorePasswd.toCharArray());

            sslContext = SslContextBuilder.forClient()
                    .keyManager(
                            (PrivateKey) keyStore.getKey(keyAlias,
                            keyStorePasswd.toCharArray()),
                            Collections.list(keyStore.aliases()).stream().map(oneAlias -> {
                                try {
                                    return (X509Certificate) keyStore.getCertificate(oneAlias);
                                } catch (KeyStoreException e) {
                                    e.printStackTrace();
                                }
                                throw new RuntimeException("There are no X509Certificates!");
                            }).toArray(X509Certificate[]::new))
                    .trustManager((X509Certificate[]) certificateCollection.toArray(new X509Certificate[0]))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().baseUrl(BASEURL).clientConnector(connector).build();
    }

}
