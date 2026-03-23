package com.dwacademy.safetysystem.member;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectRequestDto {


    private String rejectReason;

}