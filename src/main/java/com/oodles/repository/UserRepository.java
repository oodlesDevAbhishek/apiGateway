package com.oodles.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.oodles.domains.User;

@Repository
public interface UserRepository extends MongoRepository<User, String>{
	User findByEmailAndIsDeleted(String email, boolean isDeleted);
	Optional<User>findByUserId(Long userId);
}
