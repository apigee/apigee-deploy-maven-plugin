package com.apigee.mgmtapi.sdk.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "io.apigee.*")
@PropertySource("file:${configFile.path}")
public class AppConfig {

   /*
    * PropertySourcesPlaceHolderConfigurer Bean only required for @Value("{}") annotations.
    * Remove this bean if you are not using @Value annotations for injecting properties.
    */
   @Bean
   public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
       return new PropertySourcesPlaceholderConfigurer();
   }
}
