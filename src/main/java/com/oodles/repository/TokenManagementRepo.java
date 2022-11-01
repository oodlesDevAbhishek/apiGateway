package com.oodles.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.oodles.domains.TokenManagement;

@Repository
public interface TokenManagementRepo extends MongoRepository<TokenManagement, String>{
	Optional<TokenManagement> findByUserId(long userId);
}

