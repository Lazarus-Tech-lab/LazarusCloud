package ru.red.lazaruscloud.service;

import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.UserRepository;

@Service
@Data
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
         return new LazarusUserDetail(user);
    }
}
