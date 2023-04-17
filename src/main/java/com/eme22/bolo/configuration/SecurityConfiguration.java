package com.eme22.bolo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfiguration {

   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.oauth2Login()
              .tokenEndpoint().accessTokenResponseClient(new RestOAuth2AccessTokenResponseClient(restOperations()))
              .and()
              .userInfoEndpoint().userService(new RestOAuth2UserService(restOperations()));

      return http.build();
   }

   /*
   @Override
   protected void configure(HttpSecurity http) throws Exception {
      http
              .oauth2Login()
              .tokenEndpoint().accessTokenResponseClient(new RestOAuth2AccessTokenResponseClient(restOperations()))
              .and()
              .userInfoEndpoint().userService(new RestOAuth2UserService(restOperations()));
   }
   */

   @Bean
   public RestOperations restOperations() {
      return new RestTemplate();
   }

   public static final String DISCORD_BOT_USER_AGENT = "EMBot (https://github.com/eme22/PGMUSICBOT)";
}