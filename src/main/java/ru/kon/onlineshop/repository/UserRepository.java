package ru.kon.onlineshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kon.onlineshop.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
