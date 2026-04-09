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
    private final AddressHandler addressHandler;
    private final DeliveryHandler deliveryHandler;
    private final SocialHandler socialHandler;

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
                .andRoute(GET(ApiConstants.COOKS_BASE + "/me/orders"), cookHandler::getMyOrders)
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

    @Bean
    public RouterFunction<ServerResponse> addressRoutes() {
        return route(GET(ApiConstants.USERS_BASE + "/me/addresses"), addressHandler::getMyAddresses)
                .andRoute(POST(ApiConstants.USERS_BASE + "/me/addresses"), addressHandler::saveAddress)
                .andRoute(PATCH(ApiConstants.USERS_BASE + "/me/addresses/{id}"), addressHandler::updateAddress)
                .andRoute(DELETE(ApiConstants.USERS_BASE + "/me/addresses/{id}"), addressHandler::deleteAddress)
                .andRoute(POST(ApiConstants.USERS_BASE + "/me/addresses/{id}/default"), addressHandler::setDefault)
                .andRoute(GET(ApiConstants.ADDRESSES_BASE), addressHandler::getMyAddresses)
                .andRoute(POST(ApiConstants.ADDRESSES_BASE), addressHandler::saveAddress)
                .andRoute(PATCH(ApiConstants.ADDRESSES_BASE + "/{id}"), addressHandler::updateAddress)
                .andRoute(DELETE(ApiConstants.ADDRESSES_BASE + "/{id}"), addressHandler::deleteAddress)
                .andRoute(POST(ApiConstants.ADDRESSES_BASE + "/{id}/default"), addressHandler::setDefault);
    }

    @Bean
    public RouterFunction<ServerResponse> deliveryRoutes() {
        return route(GET(ApiConstants.DELIVERY_BASE + "/available"), deliveryHandler::getAvailableOrders)
                .andRoute(GET(ApiConstants.DELIVERY_BASE + "/history"), deliveryHandler::getHistory)
                .andRoute(GET(ApiConstants.DELIVERY_BASE + "/earnings"), deliveryHandler::getEarnings)
                .andRoute(POST(ApiConstants.DELIVERY_BASE + "/{orderId}/accept"), deliveryHandler::acceptDelivery)
                .andRoute(POST(ApiConstants.DELIVERY_BASE + "/{orderId}/complete"), deliveryHandler::completeDelivery)
                .andRoute(POST(ApiConstants.DELIVERY_BASE + "/{orderId}/location"), deliveryHandler::updateLocation);
    }

    @Bean
    public RouterFunction<ServerResponse> socialRoutes() {
        return route(GET(ApiConstants.USERS_BASE + "/me/favorites"), socialHandler::getMyFavorites)
                .andRoute(POST(ApiConstants.USERS_BASE + "/me/favorites/{dishId}"), socialHandler::addFavorite)
                .andRoute(DELETE(ApiConstants.USERS_BASE + "/me/favorites/{dishId}"), socialHandler::removeFavorite)
                .andRoute(GET(ApiConstants.FAVORITES_BASE), socialHandler::getMyFavorites)
                .andRoute(POST(ApiConstants.FAVORITES_BASE + "/{dishId}"), socialHandler::addFavorite)
                .andRoute(DELETE(ApiConstants.FAVORITES_BASE + "/{dishId}"), socialHandler::removeFavorite)
                .andRoute(GET(ApiConstants.USERS_BASE + "/me/following"), socialHandler::getMyFollowing)
                .andRoute(POST(ApiConstants.USERS_BASE + "/me/following/{cookId}"), socialHandler::followCook)
                .andRoute(DELETE(ApiConstants.USERS_BASE + "/me/following/{cookId}"), socialHandler::unfollowCook)
                .andRoute(GET(ApiConstants.FOLLOWS_BASE), socialHandler::getMyFollowing)
                .andRoute(POST(ApiConstants.FOLLOWS_BASE + "/{cookId}"), socialHandler::followCook)
                .andRoute(DELETE(ApiConstants.FOLLOWS_BASE + "/{cookId}"), socialHandler::unfollowCook);
    }
}
