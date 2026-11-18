
package com.sonidovivo.pagos;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.sonidovivo.pagos",
    "com.sonidovivo.security"
})
public class PagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }

    // Cliente HTTP normal: utilizado por defecto y por Eureka.
    @Bean
    @Primary
    RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(requestFactory);
    }

    // Cliente HTTP con balanceo: para comunicarse con otros microservicios.
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}


/*
package com.sonidovivo.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sonidovivo.pagos", "com.sonidovivo.security"})
public class PagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }

    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().requestFactory(requestFactory);
    }
}

*/
