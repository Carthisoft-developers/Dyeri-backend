// com/cuisinvoisin/shared/util/ApiConstants.java
package com.cuisinvoisin.shared.util;

public final class ApiConstants {
    private ApiConstants() {}

    public static final String API_BASE = "/api/v1";
    public static final String AUTH_BASE = API_BASE + "/auth";
    public static final String USERS_BASE = API_BASE + "/users";
    public static final String COOKS_BASE = API_BASE + "/cooks";
    public static final String DISHES_BASE = API_BASE + "/dishes";
    public static final String CATEGORIES_BASE = API_BASE + "/categories";
    public static final String CART_BASE = API_BASE + "/cart";
    public static final String ORDERS_BASE = API_BASE + "/orders";
    public static final String DELIVERY_BASE = API_BASE + "/delivery";
    public static final String REVIEWS_BASE = API_BASE + "/reviews";
    public static final String FAVORITES_BASE = API_BASE + "/favorites";
    public static final String FOLLOWS_BASE = API_BASE + "/follows";
    public static final String NOTIFICATIONS_BASE = API_BASE + "/notifications";
    public static final String ADDRESSES_BASE = API_BASE + "/addresses";
    public static final String SEARCH_BASE = API_BASE + "/search";

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    public static final String CART_CACHE_PREFIX = "cart:";
    public static final String REFRESH_TOKEN_PREFIX = "refresh:";
    public static final String RATE_LIMIT_PREFIX = "rl:";
}
