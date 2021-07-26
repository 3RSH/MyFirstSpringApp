package main.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletResponse;
import main.api.response.CheckLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final int PASS_CRYPT_STRENGTH = 12;
  private static final String DEFAULT_URL = "/";
  private static final String ANY_URL = "/**";
  private static final String LOGIN_URL = "/login";
  private static final String LOGOUT_URL = "/api/auth/logout";
  private static final String DELETE_COOKIE = "JSESSIONID";

  private final UserDetailsService userDetailsService;


  @Autowired
  public SecurityConfig(
      @Qualifier("UserDetailsServiceImpl") UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }


  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();

  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers(ANY_URL).permitAll()
        .anyRequest().authenticated()
        .and()
        .formLogin()
        .loginPage(LOGIN_URL)
        .defaultSuccessUrl(DEFAULT_URL, true)
        .and()
        .logout()
        .logoutUrl(LOGOUT_URL)
        .deleteCookies(DELETE_COOKIE)
        .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {

          SecurityContextHolder.clearContext();

          CheckLoginResponse response = new CheckLoginResponse();

          response.setResult(true);

          String json = new ObjectMapper().writeValueAsString(response);

          httpServletResponse.getWriter().write(json);
          httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        });

    http.exceptionHandling()
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
  }

  @Bean
  protected DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    daoAuthenticationProvider.setUserDetailsService(userDetailsService);

    return daoAuthenticationProvider;
  }

  @Bean
  protected PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(PASS_CRYPT_STRENGTH);
  }
}