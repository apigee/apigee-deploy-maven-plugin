package com.apigee.mgmtapi.sdk.core;

//@Configuration
//@ComponentScan(basePackages = "io.apigee.*")
//@PropertySource("file:${configFile.path}")
public class AppConfig {

   /*
    * PropertySourcesPlaceHolderConfigurer Bean only required for @Value("{}") annotations.
    * Remove this bean if you are not using @Value annotations for injecting properties.
    */
//   @Bean
//   public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//       return new PropertySourcesPlaceholderConfigurer();
//   }
}
