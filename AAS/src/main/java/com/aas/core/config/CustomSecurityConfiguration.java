package com.aas.core.config;


import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfiguration extends WebSecurityConfigurerAdapter {
  private static Logger authLogger = LoggerFactory.getLogger(CustomSecurityConfiguration.class);

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().withUser("Uttam-2").password("pramati123").roles("USER");
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().anyRequest().fullyAuthenticated().and().httpBasic().and().csrf().disable();
  }

  @Bean
  public RedisHandler redisHandler() {
    RedisHandler redisHandler = new RedisHandler();
    try {
      redisHandler.initialize();
    } catch (RuntimeException ex) {
      authLogger.info("Error in connecting to RedisElastic Cache from redisDiscovery: {}", ex.getLocalizedMessage());
    }

    return redisHandler;
  }
}
