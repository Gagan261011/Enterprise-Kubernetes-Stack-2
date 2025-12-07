package com.enterprise.shop.userbff.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

@Configuration
public class WebClientConfig {
    
    @Value("${middleware.service.url:http://security-middleware:8080}")
    private String middlewareUrl;
    
    @Value("${mtls.enabled:false}")
    private boolean mtlsEnabled;
    
    @Value("${mtls.keystore.path:/certs/keystore.p12}")
    private String keystorePath;
    
    @Value("${mtls.keystore.password:changeit}")
    private String keystorePassword;
    
    @Value("${mtls.truststore.path:/certs/truststore.p12}")
    private String truststorePath;
    
    @Value("${mtls.truststore.password:changeit}")
    private String truststorePassword;
    
    @Bean
    public WebClient middlewareWebClient() {
        if (mtlsEnabled) {
            return createMtlsWebClient();
        }
        return WebClient.builder()
                .baseUrl(middlewareUrl)
                .defaultHeader("X-Client-Service", "user-bff")
                .build();
    }
    
    private WebClient createMtlsWebClient() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
            
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            
            SslContext sslContext = SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .trustManager(trustManagerFactory)
                    .build();
            
            HttpClient httpClient = HttpClient.create()
                    .secure(spec -> spec.sslContext(sslContext));
            
            return WebClient.builder()
                    .baseUrl(middlewareUrl)
                    .defaultHeader("X-Client-Service", "user-bff")
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mTLS WebClient", e);
        }
    }
}
