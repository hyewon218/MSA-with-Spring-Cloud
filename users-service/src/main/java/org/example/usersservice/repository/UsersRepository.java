package org.example.usersservice.repository;

import org.example.usersservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {

}