package com.rivvystudios.portal.controller.dto;

import java.util.List;

public class LoginResponse {

    private String redirectUrl;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;

    public LoginResponse() {
    }

    public LoginResponse(String redirectUrl, String email, String firstName, String lastName, List<String> roles) {
        this.redirectUrl = redirectUrl;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
