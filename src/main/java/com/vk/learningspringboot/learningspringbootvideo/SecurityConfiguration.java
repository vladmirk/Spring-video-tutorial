package com.vk.learningspringboot.learningspringbootvideo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/", "/images/**", "/main.css", "/webjars/**").permitAll()
                .antMatchers(HttpMethod.POST, "/images").hasAnyRole("USER")
                .antMatchers("/imageMessages/**").permitAll()
//                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .csrf().ignoringAntMatchers("/console/*")
                .and()
                .headers().frameOptions().disable()
                .and().logout().logoutSuccessUrl("/");
    }

    @Autowired
    public void configureJPAUsers(AuthenticationManagerBuilder auth, SpringDataUserDetailsService detailsService) throws Exception {
        auth.userDetailsService(detailsService);
    }
}
