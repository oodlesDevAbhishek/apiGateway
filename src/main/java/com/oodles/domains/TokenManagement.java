package com.oodles.domains;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document("token_mangement")
@Data
public class TokenManagement {
	@Id
	private String id;
	@Indexed(unique = true)
	private Long userId;
	private List<TokenStatus> tokenStatus;
}
