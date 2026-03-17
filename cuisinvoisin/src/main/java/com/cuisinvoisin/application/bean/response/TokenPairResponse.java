// com/cuisinvoisin/application/bean/response/TokenPairResponse.java
package com.cuisinvoisin.application.bean.response;
public record TokenPairResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {}
