package vn.edu.fpt.cinemamanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // dùng service hiện có (optional nhưng nên có)
                .userDetailsService(userDetailsService)

                .authorizeHttpRequests(auth -> auth
                        // ✅ Static assets & public files
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**",
                                "/favicon.ico", "/error").permitAll()

                        // ✅ Trang cho guest (không cần login)
                        .requestMatchers("/", "/homepage", "/homepage/**",
                                "/movies/**", "/vouchers/**",
                                "/login", "/register").permitAll()

                        // ✅ Trang yêu cầu quyền
                        .requestMatchers("/dashboard").hasAuthority("ROLE_ADMIN")
                        // Lưu ý: sửa tên quyền cho đúng với DB của bạn
                        .requestMatchers("/staff_home")
                        .hasAnyAuthority("ROLE_CASHIER_STAFF", "ROLE_REDEMPTION_STAFF")

                        // Các URL khác thì cần đăng nhập
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/homepage", true) // sau login luôn về homepage
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // 👉 nếu muốn user sau khi logout vẫn vào được trang guest, cho về homepage
                        .logoutSuccessUrl("/homepage")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                )

                // tuỳ nhu cầu API/form, tạm tắt cho đơn giản
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
