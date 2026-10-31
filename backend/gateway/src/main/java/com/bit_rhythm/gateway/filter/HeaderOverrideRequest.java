package com.bit_rhythm.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Hides the given incoming headers and replaces/adds others, so that everything after this
 * wrapper (including the proxy handler) only sees trusted values.
 */
public final class HeaderOverrideRequest extends HttpServletRequestWrapper {

    private final Set<String> removed = new LinkedHashSet<>();
    private final Map<String, String> added = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public HeaderOverrideRequest(HttpServletRequest request, Collection<String> removeNames,
                                 Map<String, String> setHeaders) {
        super(request);
        removeNames.forEach(name -> removed.add(name.toLowerCase(Locale.ROOT)));
        added.putAll(setHeaders);
    }

    @Override
    public String getHeader(String name) {
        if (added.containsKey(name)) {
            return added.get(name);
        }
        return removed.contains(name.toLowerCase(Locale.ROOT)) ? null : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (added.containsKey(name)) {
            return Collections.enumeration(Set.of(added.get(name)));
        }
        return removed.contains(name.toLowerCase(Locale.ROOT)) ? Collections.emptyEnumeration() : super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> original = super.getHeaderNames();
        while (original != null && original.hasMoreElements()) {
            String name = original.nextElement();
            if (!removed.contains(name.toLowerCase(Locale.ROOT))) {
                names.add(name);
            }
        }
        names.addAll(added.keySet());
        return Collections.enumeration(names);
    }
}
