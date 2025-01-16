package com.example.project.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Builder
@Table(name = "users")
public class User extends BaseEntity 
{
	@Email
	@NotNull
	@Column(name = "email", nullable = false, unique = true)
	private String email;
}


