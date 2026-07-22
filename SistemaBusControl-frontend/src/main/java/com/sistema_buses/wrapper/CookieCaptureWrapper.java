package com.sistema_buses.wrapper;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class CookieCaptureWrapper extends HttpServletResponseWrapper {
    private final List<Cookie> capturedCookies = new ArrayList<>();

    public CookieCaptureWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void addCookie(Cookie cookie) {
        // Capture the cookie locally
        capturedCookies.add(cookie);
        // Pass it through to the real response
        super.addCookie(cookie);
    }

    public List<Cookie> getCapturedCookies() {
        return capturedCookies;
    }
}