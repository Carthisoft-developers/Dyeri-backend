// com/dyeri/core/interfaces/rest/routers/ApiRouter.java
package com.dyeri.core.interfaces.rest.routers;

import com.dyeri.core.interfaces.rest.handlers.*;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class ApiRouter {

    private final UserHandler userHandler;
    private final CookHandler cookHandler;
    private final CatalogueHandler catalogueHandler;
    private final CartHandler cartHandler;
    private final OrderHandler orderHandler;

    @Bean
    public RouterFunction<ServerResponse> userRoutes() {
        return route(POST(ApiConstants.USERS_BASE + "/register"), userHandler::register)
                .andRoute(GET(ApiConstants.USERS_BASE + "/me"), userHandler::getMe)
                .andRoute(PATCH(ApiConstants.USERS_BASE + "/me"), userHandler::updateMe)
                .andRoute(POST(ApiConstants.USERS_BASE + "/me/avatar"), userHandler::uploadAvatar)
                .andRoute(GET(ApiConstants.USERS_BASE + "/{id}/avatar"), userHandler::getAvatarById);
    }

    @Bean
    public RouterFunction<ServerResponse> cookRoutes() {
        return route(GET(ApiConstants.COOKS_BASE), cookHandler::getNearbyCooks)
                .andRoute(GET(ApiConstants.COOKS_BASE + "/me"), cookHandler::getMyProfile)
                .andRoute(GET(ApiConstants.COOKS_BASE + "/me/dashboard"), cookHandler::getDashboard)
                .andRoute(GET(ApiConstants.COOKS_BASE + "/{id}"), cookHandler::getCook)
                .andRoute(GET(ApiConstants.COOKS_BASE + "/{id}/reviews"), cookHandler::getCookReviews)
                .andRoute(PATCH(ApiConstants.COOKS_BASE + "/me"), cookHandler::updateMyProfile);
    }

    @Bean
    public RouterFunction<ServerResponse> catalogueRoutes() {
        return route(GET(ApiConstants.CATEGORIES_BASE), catalogueHandler::getCategories)
                .andRoute(POST(ApiConstants.CATEGORIES_BASE), catalogueHandler::createCategory)
                .andRoute(GET(ApiConstants.DISHES_BASE), catalogueHandler::getDishes)
                .andRoute(GET(ApiConstants.DISHES_BASE + "/{id}"), catalogueHandler::getDish)
                .andRoute(GET(ApiConstants.DISHES_BASE + "/{id}/image"), catalogueHandler::getDishImage)
                .andRoute(POST(ApiConstants.DISHES_BASE), catalogueHandler::createDish)
                .andRoute(POST(ApiConstants.DISHES_BASE + "/{id}/image"), catalogueHandler::uploadDishImage)
                .andRoute(PATCH(ApiConstants.DISHES_BASE + "/{id}"), catalogueHandler::updateDish)
                .andRoute(DELETE(ApiConstants.DISHES_BASE + "/{id}"), catalogueHandler::deleteDish)
                .andRoute(PATCH(ApiConstants.DISHES_BASE + "/{id}/toggle"), catalogueHandler::toggleAvailability);
    }

    @Bean
    public RouterFunction<ServerResponse> cartRoutes() {
        return route(GET(ApiConstants.CART_BASE), cartHandler::getCart)
                .andRoute(POST(ApiConstants.CART_BASE + "/items"), cartHandler::addItem)
                .andRoute(PATCH(ApiConstants.CART_BASE + "/items/{itemId}"), cartHandler::updateItem)
                .andRoute(DELETE(ApiConstants.CART_BASE + "/items/{itemId}"), cartHandler::removeItem)
                .andRoute(DELETE(ApiConstants.CART_BASE), cartHandler::clearCart);
    }

    @Bean
    public RouterFunction<ServerResponse> orderRoutes() {
        return route(POST(ApiConstants.ORDERS_BASE), orderHandler::placeOrder)
                .andRoute(GET(ApiConstants.ORDERS_BASE), orderHandler::getOrders)
                .andRoute(GET(ApiConstants.ORDERS_BASE + "/{id}"), orderHandler::getOrder)
                .andRoute(PATCH(ApiConstants.ORDERS_BASE + "/{id}/status"), orderHandler::updateStatus)
                .andRoute(DELETE(ApiConstants.ORDERS_BASE + "/{id}/cancel"), orderHandler::cancelOrder);
    }
}
