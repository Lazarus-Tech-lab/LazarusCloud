package ru.red.lazaruscloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.red.lazaruscloud.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
     Optional<User> findUserByUsername(String username);
     boolean existsByUsername(String username);
     boolean existsByEmail(String email);
}
