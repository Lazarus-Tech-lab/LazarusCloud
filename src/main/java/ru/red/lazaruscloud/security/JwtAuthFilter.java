package ru.red.lazaruscloud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.red.lazaruscloud.service.UserDetailService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenHelper tokenHelper;
    private final UserDetailService userDetailService;

//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//
//        return path.startsWith("/style/")
//                || path.startsWith("/script/")
//                || path.equals("/")
//                || path.equals("/index.html")
//                || path.equals("/auth.html")
//                || path.endsWith(".css")
//                || path.endsWith(".js")
//                || path.endsWith(".html")
//                || path.endsWith(".ico");
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Пробуем получить токен из разных источников
        String token = getTokenFromRequest(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Валидация токена
        Claims claims;
        try {
            claims = tokenHelper.getClaimsFromToken(token);

            // Проверяем тип токена (access/refresh)
            if (!"access".equals(claims.get("type", String.class))) {
                sendError(response, "Invalid token type", HttpStatus.FORBIDDEN);
                return;
            }
        } catch (JwtException | IllegalArgumentException e) {
            sendError(response, "Invalid token", HttpStatus.UNAUTHORIZED);
            return;
        }

        // 3. Установка аутентификации в контекст
        String username = claims.getSubject();
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailService.loadUserByUsername(username);

            // Дополнительная проверка валидности пользователя
            if (userDetails.isEnabled()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. Проверяем Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Проверяем параметр запроса (для WebSocket или особых случаев)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null) {
            return tokenParam;
        }

        // 3. Проверяем куки (если используется)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void sendError(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        Cookie cookie = new Cookie("access_token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Удалить
        response.addCookie(cookie);

        response.setContentType("application/json");
        response.setStatus(status.value());
        response.getWriter().write(
                String.format("{\"error\": \"%s\", \"message\": \"%s\"}", status.getReasonPhrase(), message)
        );
    }

}