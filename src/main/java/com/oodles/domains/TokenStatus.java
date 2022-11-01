package com.oodles.domains;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenStatus {
	private String tokenId;
	private LocalDateTime issueTime;
	private boolean valid;
}
