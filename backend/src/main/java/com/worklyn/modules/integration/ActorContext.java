package com.worklyn.modules.integration;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.lang.reflect.Method;
import java.util.*;

/** Reads identity from Spring Security without coupling Person 2 to Person 1's UserPrincipal implementation. */
public record ActorContext(String userId, String employeeId, Set<String> roles) {
    public boolean hasAnyRole(String... expected) { return Arrays.stream(expected).anyMatch(roles::contains); }
    public boolean isHrOrAdmin() { return hasAnyRole("ADMIN", "HR"); }
    public boolean canApproveLeaves() { return hasAnyRole("ADMIN", "HR", "MANAGER", "MGR"); }
    public static ActorContext from(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        Set<String> roles = new HashSet<>();
        for (GrantedAuthority a : auth.getAuthorities()) {
            String r = a.getAuthority(); roles.add(r.startsWith("ROLE_") ? r.substring(5) : r);
        }
        Object principal = auth.getPrincipal();
        String empId = extract(principal, "empId", "getEmpId", "getEmployeeId", "employeeId");
        if (empId == null && principal instanceof Map<?,?> m) empId = string(m.get("empId"));
        if (empId == null) empId = auth.getName();
        return new ActorContext(auth.getName(), empId, Set.copyOf(roles));
    }
    private static String extract(Object obj, String... names) {
        if (obj == null) return null;
        if (obj instanceof Map<?,?> m) for (String n : names) { String key=n.startsWith("get")?Character.toLowerCase(n.charAt(3))+n.substring(4):n; String v=string(m.get(key)); if(v!=null)return v; }
        for (String n : names) if (n.startsWith("get")) try { Method m=obj.getClass().getMethod(n); String v=string(m.invoke(obj)); if(v!=null)return v; } catch (ReflectiveOperationException ignored) { }
        return null;
    }
    private static String string(Object value) { return value == null || value.toString().isBlank() ? null : value.toString(); }
}
