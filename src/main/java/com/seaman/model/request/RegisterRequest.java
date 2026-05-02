package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class RegisterRequest {

    @NotBlank(message = "First name missing parameter.")
    private String firstName;

    @NotBlank(message =  "Last name missing parameter.")
    private String lastName;

    private String companyCode;

    @NotBlank(message =  "Position missing parameter.")
    private String positionCode;

    @NotBlank(message =  "Email missing parameter.")
    private String email;

    @NotBlank(message =  "Password missing parameter.")
    private String password;

    @NotBlank(message =  "Mobile missing parameter.")
    private String mobileNumber;
}
