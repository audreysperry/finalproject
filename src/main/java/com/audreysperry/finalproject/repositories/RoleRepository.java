package com.audreysperry.finalproject.repositories;

import com.audreysperry.finalproject.models.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByName(String roleName);

}
