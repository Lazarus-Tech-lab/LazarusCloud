package ru.red.lazaruscloud.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HtmlBlockFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/index.html".equals(request.getRequestURI()) && !"/auth.html".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getDispatcherType() != DispatcherType.FORWARD) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Direct access to index.html is forbidden");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
